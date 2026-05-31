package com.example.sincra.database.repositorio;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.database.dao.UserDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.PredictSettimana;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.User;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CicloRepository {

    private CicloDAO dao;
    private RegistrazioneDAO daoRe;
    private UserDAO daoUser;
    private ExecutorService executor;
    private final Context context;

    public CicloRepository(Context context){
        this.context = context;
        dao = AppDatabase.getDatabase(context).cicloDAO();
        daoRe = AppDatabase.getDatabase(context).registrazioneDAO();
        daoUser = AppDatabase.getDatabase(context).userDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    public long getLocalId() {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getLong("local_user_id", -1L);
    }

    public LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni() {
        return dao.getCicliConRegistrazioni(getLocalId());
    }

    public LiveData<List<Ciclo>> getHistorialCicli(){
        return dao.getHistorialCicli(getLocalId());
    }

    // =========================================================================
    // ACCIÓN PRINCIPAL: MARCAR O DESMARCAR UN DÍA DE REGLA
    // =========================================================================
    public void addOrDeletePeriodDay(Date dataInput) {
        executor.execute(() -> {
            Date data = truncarFecha(dataInput);
            Registrazione regActual = daoRe.getRegistroByDate(data, getLocalId());
            Registrazione regPrev = daoRe.getRegistroByDate(xDay(data, -1), getLocalId());
            Registrazione regNext = daoRe.getRegistroByDate(xDay(data, 1), getLocalId());

            if (regActual == null) {
                // Si no existe, lo creamos temporalmente desvinculado (cicloId = null)
                regActual = new Registrazione(data, false, false, 1, null, null, 0);
            }

            boolean isPeriodoActualmente = regActual.isPeriodo();

            // Invertimos el estado
            regActual.setPeriodo(!isPeriodoActualmente);
            long id = daoRe.insertOrUpdate(regActual);
            regActual.setRegistroId((int) id);

            if (!isPeriodoActualmente) {
                // CASO: AÑADIR REGLA
                handleAddPeriod(data, regActual, regPrev, regNext);
            } else {
                // CASO: QUITAR REGLA
                handleRemovePeriod(data, regActual, regPrev, regNext);
            }
        });
    }

    // =========================================================================
    // LÓGICA AL AÑADIR REGLA
    // =========================================================================
    private void handleAddPeriod(Date data, Registrazione regActual, Registrazione regPrev, Registrazione regNext) {
        boolean prevHuboRegla = (regPrev != null && regPrev.isPeriodo());
        boolean nextHuboRegla = (regNext != null && regNext.isPeriodo());

        // 1. FUSIONAR (Merge): Tapamos un hueco entre dos días de regla
        if (prevHuboRegla && nextHuboRegla) {
            Ciclo cicloAnterior = dao.getByIdSync(regPrev.getCicloId());
            Ciclo cicloSiguiente = dao.getByIdSync(regNext.getCicloId());

            if (cicloAnterior != null && cicloSiguiente != null && cicloAnterior.getCicloId() != cicloSiguiente.getCicloId()) {
                // Unimos el ciclo siguiente dentro del anterior
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + cicloSiguiente.getDurataPeriodo() + 1);
                cicloAnterior.setDataFine(cicloSiguiente.getDataFine());
                cicloAnterior.setDurataTotale((int) difDays(cicloAnterior.getDataInizio(), cicloAnterior.getDataFine()));
                dao.update(cicloAnterior);

                // Reasignar todos los registros del ciclo siguiente al ciclo anterior
                List<Registrazione> registrosSiguientes = daoRe.getRegistrosPosterioresSync(cicloSiguiente.getCicloId(), cicloSiguiente.getDataInizio(), getLocalId());
                if (registrosSiguientes != null) {
                    for (Registrazione r : registrosSiguientes) {
                        r.setCicloId(cicloAnterior.getCicloId());
                        daoRe.insertOrUpdate(r);
                    }
                }

                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
                dao.delete(cicloSiguiente); // Borramos el ciclo que quedó obsoleto
                updatePeriodDays(cicloAnterior);
            } else if (cicloAnterior != null) {
                // Ya pertenecían al mismo ciclo, solo actualizamos duración
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + 1);
                dao.update(cicloAnterior);
                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
            }
        }
        // 2. AÑADIR AL PRINCIPIO (Prepend): Seleccionamos un día justo antes del inicio de un ciclo
        else if (!prevHuboRegla && nextHuboRegla) {
            Ciclo cicloSiguiente = dao.getByIdSync(regNext.getCicloId());
            if (cicloSiguiente != null) {
                cicloSiguiente.setDataInizio(data); // Movemos el inicio hacia atrás
                cicloSiguiente.setDurataPeriodo(cicloSiguiente.getDurataPeriodo() + 1);
                if (cicloSiguiente.getDataFine() != null) {
                    cicloSiguiente.setDurataTotale((int) difDays(data, cicloSiguiente.getDataFine()));
                }
                dao.update(cicloSiguiente);

                regActual.setCicloId(cicloSiguiente.getCicloId());
                daoRe.insertOrUpdate(regActual);
                updatePeriodDays(cicloSiguiente);
            }
        }
        // 3. AÑADIR AL FINAL (Append): Seleccionamos un día justo después del final del sangrado
        else if (prevHuboRegla && !nextHuboRegla) {
            Ciclo cicloAnterior = dao.getByIdSync(regPrev.getCicloId());
            if (cicloAnterior != null) {
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + 1);
                dao.update(cicloAnterior);

                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
            }
        }
        // 4. NUEVO CICLO: Un día aislado
        else {
            Ciclo cicloAnteriorMasCercano = dao.getCicloAnteriorSync(data, getLocalId());
            Ciclo cicloPosterior = dao.getCicloPosteriorSync(data, getLocalId());

            Date dataFineNuevo = (cicloPosterior != null) ? xDay(cicloPosterior.getDataInizio(), -1) : null;

            Ciclo nuevoCiclo = new Ciclo(data, dataFineNuevo, 28, 1, getLocalId());
            if (dataFineNuevo != null) {
                nuevoCiclo.setDurataTotale((int) difDays(data, dataFineNuevo));
            }

            long nuevoCicloId = dao.insert(nuevoCiclo);
            regActual.setCicloId((int) nuevoCicloId);
            daoRe.insertOrUpdate(regActual);

            // Cerrar el ciclo anterior correctamente
            if (cicloAnteriorMasCercano != null && (cicloAnteriorMasCercano.getDataFine() == null || cicloAnteriorMasCercano.getDataFine().after(data))) {
                closePrevCiclo(cicloAnteriorMasCercano, xDay(data, -1));
            }

            Ciclo nuevoCargado = dao.getByIdSync((int) nuevoCicloId);
            if (nuevoCargado != null) updatePeriodDays(nuevoCargado);
        }
    }

    // =========================================================================
    // LÓGICA AL QUITAR REGLA
    // =========================================================================
    private void handleRemovePeriod(Date data, Registrazione regActual, Registrazione regPrev, Registrazione regNext) {
        Ciclo cicloActual = dao.getByIdSync(regActual.getCicloId());
        if (cicloActual == null) return;

        boolean prevHuboRegla = (regPrev != null && regPrev.isPeriodo() && regPrev.getCicloId() == cicloActual.getCicloId());
        boolean nextHuboRegla = (regNext != null && regNext.isPeriodo() && regNext.getCicloId() == cicloActual.getCicloId());

        cicloActual.setDurataPeriodo(Math.max(0, cicloActual.getDurataPeriodo() - 1));

        // 1. ELIMINAR: Era el único día de regla
        if (cicloActual.getDurataPeriodo() == 0) {
            Ciclo cicloAnterior = dao.getCicloAnteriorSync(cicloActual.getDataInizio(), getLocalId());
            List<Registrazione> todosLosDelCiclo = daoRe.getRegistrosPosterioresSync(cicloActual.getCicloId(), cicloActual.getDataInizio(), getLocalId());

            if (cicloAnterior != null) {
                // Reasignar días al ciclo anterior
                if (todosLosDelCiclo != null) {
                    for (Registrazione r : todosLosDelCiclo) {
                        r.setCicloId(cicloAnterior.getCicloId());
                        r.setPeriodo(false);
                        daoRe.insertOrUpdate(r);
                    }
                }
                // Expandir ciclo anterior
                Date nuevaDataFine = cicloActual.getDataFine();
                cicloAnterior.setDataFine(nuevaDataFine);
                if (nuevaDataFine != null) {
                    cicloAnterior.setDurataTotale((int) difDays(cicloAnterior.getDataInizio(), nuevaDataFine));
                }
                dao.update(cicloAnterior);
                dao.delete(cicloActual);
                updatePeriodDays(cicloAnterior);
            } else {
                // No hay ciclo anterior, dejar registros huérfanos o en ciclo null
                if (todosLosDelCiclo != null) {
                    for (Registrazione r : todosLosDelCiclo) {
                        r.setCicloId(null);
                        r.setGiornoCiclo(0);
                        r.setPeriodo(false);
                        daoRe.insertOrUpdate(r);
                    }
                }
                dao.delete(cicloActual);
            }
        }
        // 2. QUITAR EL PRIMERO: Desplazar inicio hacia adelante
        else if (!prevHuboRegla && nextHuboRegla) {
            Date nuevaDataInizio = xDay(data, 1);
            cicloActual.setDataInizio(nuevaDataInizio);
            if (cicloActual.getDataFine() != null) {
                cicloActual.setDurataTotale((int) difDays(nuevaDataInizio, cicloActual.getDataFine()));
            }
            dao.update(cicloActual);

            // El día que borramos se lo damos al ciclo anterior si existe
            Ciclo cicloAnterior = dao.getCicloAnteriorSync(data, getLocalId());
            if (cicloAnterior != null) {
                regActual.setCicloId(cicloAnterior.getCicloId());
                cicloAnterior.setDataFine(data);
                cicloAnterior.setDurataTotale((int) difDays(cicloAnterior.getDataInizio(), data));
                dao.update(cicloAnterior);
            } else {
                regActual.setCicloId(null);
                regActual.setGiornoCiclo(0);
            }
            daoRe.insertOrUpdate(regActual);
            updatePeriodDays(cicloActual);
        }
        // 3. QUITAR EL ÚLTIMO: Solo se reduce la duración del sangrado
        else if (prevHuboRegla && !nextHuboRegla) {
            dao.update(cicloActual);
        }
        // 4. PARTIR (Split): Hemos quitado un día en medio de un sangrado
        else if (prevHuboRegla && nextHuboRegla) {
            // Nota: Para simplificar y evitar fallos críticos, muchas apps
            // no permiten "huecos" en el sangrado, o simplemente asumen que todo es el mismo ciclo.
            // Si quieres permitir partirlo, crearías un nuevo ciclo a partir de 'nextDay'.
            // Aquí optamos por mantenerlo en el mismo ciclo, solo que ese día no es sangrado.
            dao.update(cicloActual);
        }
    }

    // =========================================================================
    // MÉTODOS DE RECALCULO DE DÍAS (SÍNCRO)
    // =========================================================================
    public void closePrevCiclo(Ciclo ciclo, Date dataFine){
        Date fechaFinTruncada = truncarFecha(dataFine);
        ciclo.setDataFine(fechaFinTruncada);
        ciclo.setDurataTotale((int) difDays(ciclo.getDataInizio(), fechaFinTruncada));
        dao.update(ciclo);
        updatePeriodDays(ciclo);
        updateDurataMedia();
    }

    public void updateDurataMedia(){
        executor.execute(() -> {
            User user = daoUser.getByIdAsync(getLocalId());
            if (user == null) return;

            int durataCiclo = calcoloMediaCicloUltimi4Cicli();
            int durataPeriodo = calcoloMediaPeriodoUltimi4Cicli();
            if (durataCiclo > 0) user.setDurataMediaCiclo(durataCiclo);
            if (durataPeriodo > 0) user.setDurataMediaPeriodo(durataPeriodo);

            daoUser.update(user);
        });
    }

    private void updatePeriodDays(Ciclo ciclo) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ciclo.getDataInizio());
        Date dataEv = truncarFecha(cal.getTime());
        Date fechaFin = (ciclo.getDataFine() != null) ? truncarFecha(ciclo.getDataFine()) : truncarFecha(new Date());

        int i = 1;
        while (!dataEv.after(fechaFin)) {
            Registrazione reg = daoRe.getRegistroByDate(dataEv, getLocalId());
            if (reg == null) {
                reg = new Registrazione(dataEv, false, false, i, null, ciclo.getCicloId(), 0);
            }
            // Asegurarse de que el día pertenece a este ciclo antes de reescribir su giorno
            if (reg.getCicloId() == null || reg.getCicloId() == ciclo.getCicloId()) {
                reg.setCicloId(ciclo.getCicloId());
                reg.setGiornoCiclo(i);
                daoRe.insertOrUpdate(reg);
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dataEv = truncarFecha(cal.getTime());
            i++;
        }
    }

    private int calcoloMediaPeriodoUltimi4Cicli(){
        List<Ciclo> ultimi = dao.getLastFourCicliSync(getLocalId());
        if (ultimi == null || ultimi.isEmpty()){
            return 0;
        }
        int sumDays = 0;
        for (Ciclo c: ultimi){
            sumDays += c.getDurataPeriodo();
        }
        return Math.round( (float) sumDays / ultimi.size());
    }

    private int calcoloMediaCicloUltimi4Cicli(){
        List<Ciclo> ultimi = dao.getLastFourCicliSync(getLocalId());
        if (ultimi == null || ultimi.isEmpty()){
            return 0;
        }
        int sumDays = 0;
        for (Ciclo c: ultimi){
            sumDays += c.getDurataTotale();
        }
        return Math.round((float) sumDays / ultimi.size());
    }

    public void calcoloGiorniProbabile(Date inizioUltimoCiclo, PredictionsCallback callback) {
        executor.execute(() -> {
            List<Date> giorni = new ArrayList<>();
            User user = daoUser.getByIdAsync(getLocalId());
            Date iterCiclo = inizioUltimoCiclo;

            for (int ciclo = 0; ciclo <3; ciclo ++){
                iterCiclo = xDay(iterCiclo, user.getDurataMediaCiclo());
                Date iter = iterCiclo;
                for (int i=0; i< user.getDurataMediaPeriodo(); i++){
                    giorni.add(iter);
                    iter = xDay(iter, 1);

                }
            }
            callback.onResult(giorni);
        });
    }

    public interface PredictionsCallback {
        void onResult(List<Date> fechasProbables);
    }

    // =========================================================================
    // UTILS Y TRUNCADOS
    // =========================================================================
    public static Date truncarFecha(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date xDay(Date data, int x) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.add(Calendar.DAY_OF_MONTH, x);
        return truncarFecha(calendar.getTime());
    }

    public static int difDays(Date dataInizio, Date dataFine) {
        Date d1 = truncarFecha(dataInizio);
        Date d2 = truncarFecha(dataFine);
        long diffMillis = d2.getTime() - d1.getTime();
        // NOTA: Algunas apps suman +1 aquí para que un ciclo del 1 al 28 dure 28 días, no 27.
        return (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
    }

    public void generatePredictions(PredictionCallback callback) {
        executor.execute(() -> {
            List<Ciclo> historial = dao.getHistorialCicliSync(getLocalId());
            List<PredictSettimana> resultadoPredicciones = new ArrayList<>();
            callback.onPredictionsGenerated(resultadoPredicciones);
        });
    }

    public interface PredictionCallback {
        void onPredictionsGenerated(List<PredictSettimana> predictions);
    }

    public LiveData<CicloConRegistrazioni> getCicloActual(){ return dao.getCicloActualConRegistrazioni(getLocalId()); }
    public LiveData<CicloConRegistrazioni> getCicloByIdConRegistrazioni(int cicloId){ return dao.getCicloByIdConRegistrazioni(cicloId); }

    public LiveData<List<Date>> getFechasConPeriodo() {
        return daoRe.getFechasConPeriodoByUserId(getLocalId());
    }
}
