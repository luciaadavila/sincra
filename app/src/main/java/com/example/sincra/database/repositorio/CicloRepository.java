package com.example.sincra.database.repositorio;

import static com.google.android.material.datepicker.DateValidatorPointBackward.before;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.PredictSettimana;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CicloRepository {

    private CicloDAO dao;
    private RegistrazioneDAO daoRe;
    private ExecutorService executor;

    private final Context context;

    public CicloRepository(Context context){
        this.context = context;
        dao = AppDatabase.getDatabase(context).cicloDAO();
        daoRe = AppDatabase.getDatabase(context).registrazioneDAO();
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

    public void addOrDeletePeriodDay(Date dataInput) {
        executor.execute(() -> {
            Date data = truncarFecha(dataInput);
            Registrazione registrazioneDate = daoRe.getRegistroByDate(data, getLocalId());
            Registrazione registrazionePrevDate = daoRe.getRegistroByDate(prevDay(data), getLocalId());
            Registrazione registrazioneNextDate = daoRe.getRegistroByDate(nextDay(data), getLocalId());

            Ciclo currentCiclo = dao.getCurrentCiclo(getLocalId());

            // si todavía no había una registración, significa que el día no era de regla
            // creamos registrazione y la insertamos
            if (registrazioneDate == null){
                int cicloId = (currentCiclo != null) ? currentCiclo.getCicloId() : 0;
                registrazioneDate = new Registrazione(data, false, false, 1, null, cicloId, 0);
            }
            boolean isPeriodDay = registrazioneDate.isPeriodo();

            if (isPeriodDay){
                handleDeletePeriodo(data, registrazionePrevDate, registrazioneNextDate, currentCiclo);
                return;
            }

            // Si llegamos aquí es que queremos añadir el día de periodo
            registrazioneDate.setPeriodo(true);

            boolean wasPeriodDay = (registrazionePrevDate != null && registrazionePrevDate.isPeriodo());
            if (!wasPeriodDay){ // si el día anterior no había regla y ahora sí -> empieza nuevo ciclo
                if (currentCiclo != null && currentCiclo.getDataFine() == null){
                    closePrevCiclo(currentCiclo, prevDay(data));
                }

                Ciclo nuevoCiclo = new Ciclo(data, null, 28, 5, getLocalId());
                long nuevoCicloId = dao.insert(nuevoCiclo);
                registrazioneDate.setCicloId((int) nuevoCicloId);
                daoRe.insertOrUpdate(registrazioneDate);

            } else {
                if (currentCiclo != null){
                    registrazioneDate.setCicloId(currentCiclo.getCicloId());
                } else {
                    // Si no hay ciclo actual pero el anterior tenía periodo, creamos uno nuevo por seguridad
                    Ciclo nuevoCiclo = new Ciclo(data, null, 28, 5, getLocalId());
                    long id = dao.insert(nuevoCiclo);
                    registrazioneDate.setCicloId((int) id);
                }
                daoRe.insertOrUpdate(registrazioneDate);
            }
        });
    }

    public void closePrevCiclo(Ciclo ciclo, Date dataFine){
        ciclo.setDataFine(dataFine);
        ciclo.setDurataTotale((int) difDays(ciclo.getDataInizio(), ciclo.getDataFine()));
        dao.update(ciclo);

        // RELLENAR REGISTRACIONES INTERMEDIAS
        Calendar cal = Calendar.getInstance();
        cal.setTime(ciclo.getDataInizio());

        Date dataEv = ciclo.getDataInizio();

        int i = 1;
        while (!dataEv.after(dataFine)){
            Registrazione reg = daoRe.getRegistroByDate(dataEv, getLocalId());
            if (reg == null){
                reg = new Registrazione(dataEv, false, false, i, null, ciclo.getCicloId(), 0);
            }
            reg.setCicloId(ciclo.getCicloId());
            reg.setGiornoCiclo(i);
            daoRe.insertOrUpdate(reg);

            cal.add(Calendar.DAY_OF_MONTH, 1);
            dataEv = cal.getTime();
            i++;
        }
    }

    private void handleDeletePeriodo(Date data, Registrazione prevReg, Registrazione nextReg, Ciclo currentCiclo){
        Registrazione reg = daoRe.getRegistroByDate(data, getLocalId());
        if (reg == null || reg.getCicloId() == 0) return;

        int cicloIdOriginal = reg.getCicloId();
        Ciclo cicloOriginal = dao.getByIdSync(cicloIdOriginal);
        if (cicloOriginal == null ) return;

        // Desmarcamos el día de regla
        reg.setPeriodo(false);
        daoRe.insertOrUpdate(reg);

        boolean wasPeriodDay = (prevReg != null && prevReg.isPeriodo());
        boolean isNextPeriodDay = (nextReg != null && nextReg.isPeriodo());

        // =========================================================================
        // ESCENARIO 1: DIVIDIR EL CICLO (Ayer REGLA y Mañana REGLA)
        // =========================================================================
        if (wasPeriodDay && isNextPeriodDay){
            // Cerramos el ciclo original en el día de ayer
            closePrevCiclo(cicloOriginal, prevDay(data));

            // Creamos nuevo ciclo que empieza mañana (con el primer día de regla restante)
            Ciclo nuevoCiclo = new Ciclo(nextDay(data), null, 28, 5, getLocalId());
            long nuevoCicloId = dao.insert(nuevoCiclo);

            List<Registrazione> posteriores = daoRe.getRegistrosPosterioresSync(cicloOriginal.getCicloId(), nextDay(data), getLocalId());
            if (posteriores != null){
                for (Registrazione r: posteriores){
                    r.setCicloId((int) nuevoCicloId);
                    daoRe.insertOrUpdate(r);
                }
            }

            updatePeriodDays(cicloOriginal);
            Ciclo nuevoCicloCargado = dao.getByIdSync((int) nuevoCicloId);
            if (nuevoCicloCargado != null){
                updatePeriodDays(nuevoCicloCargado);
            }
        }
        // =========================================================================
        // ESCENARIO 2: RETRASAR EL INICIO (Ayer NO REGLA y Mañana REGLA)
        // =========================================================================
        else if (!wasPeriodDay && isNextPeriodDay) {
            // El ciclo ya no empieza el día 'data', ahora empieza mañana
            Date fechaManana = nextDay(data);
            cicloOriginal.setDataInizio(fechaManana);
            if (cicloOriginal.getDataFine() != null) {
                cicloOriginal.setDurataTotale((int) difDays(fechaManana, cicloOriginal.getDataFine()));
            }
            dao.update(cicloOriginal);

            updatePeriodDays(cicloOriginal);
        }
        // =========================================================================
        // ESCENARIO 3: UNIFICAR / ELIMINAR CICLO (Ayer NO REGLA y Mañana NO REGLA)
        // =========================================================================
        else if (!wasPeriodDay && !isNextPeriodDay) {
            // Buscamos si existe un ciclo anterior en el historial para absorber estos días huérfanos
            Ciclo cicloAnterior = dao.getCicloAnteriorSync(cicloOriginal.getDataInizio(), getLocalId());

            List<Registrazione> todosLosDelCiclo = daoRe.getRegistrosPosterioresSync(cicloIdOriginal, cicloOriginal.getDataInizio(), getLocalId());

            if (cicloAnterior != null) {
                // Pasamos todos los días de este ciclo eliminado al ciclo anterior
                if (todosLosDelCiclo != null) {
                    for (Registrazione r : todosLosDelCiclo) {
                        r.setCicloId(cicloAnterior.getCicloId());
                        daoRe.insertOrUpdate(r);
                    }

                    // Si el ciclo anterior ya tenía fecha de fin, la extendemos hasta absorber el último día movido
                    if (cicloAnterior.getDataFine() != null && !todosLosDelCiclo.isEmpty()) {
                        Date ultimaFecha = todosLosDelCiclo.get(todosLosDelCiclo.size() - 1).getDate();
                        cicloAnterior.setDataFine(ultimaFecha);
                        cicloAnterior.setDurataTotale((int) difDays(cicloAnterior.getDataInizio(), ultimaFecha));
                        dao.update(cicloAnterior);
                    }
                }

                // Eliminamos de la base de datos el ciclo actual redundante
                dao.delete(cicloOriginal);
                updatePeriodDays(cicloAnterior);

            } else {
                // Si no hay ciclo anterior en el historial, desvinculamos los registros (cicloId = 0)
                if (todosLosDelCiclo != null) {
                    for (Registrazione r : todosLosDelCiclo) {
                        r.setCicloId(0);
                        r.setGiornoCiclo(0);
                        daoRe.insertOrUpdate(r);
                    }
                }
                dao.delete(cicloOriginal);
            }
        }
    }

    private void updatePeriodDays(Ciclo ciclo) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ciclo.getDataInizio());
        Date dataEv = truncarFecha(cal.getTime());
        Date fechaFin = (ciclo.getDataFine() != null) ? ciclo.getDataFine() : truncarFecha(new Date());

        int i = 1;
        while (!dataEv.after(fechaFin)) {
            Registrazione reg = daoRe.getRegistroByDate(dataEv, getLocalId());
            if (reg != null && reg.getCicloId() == ciclo.getCicloId()) {
                reg.setGiornoCiclo(i);
                daoRe.insertOrUpdate(reg);
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dataEv = truncarFecha(cal.getTime());
            i++;
        }
    }

    public interface PredictionCallback {
        void onPredictionsGenerated(List<PredictSettimana> predictions);
    }

    public void generatePredictions(PredictionCallback callback) {
        executor.execute(() -> {
            List<Ciclo> historial = dao.getHistorialCicliSync(getLocalId());
            List<PredictSettimana> resultadoPredicciones = new ArrayList<>();
            callback.onPredictionsGenerated(resultadoPredicciones);
        });
    }

    public void insert(Ciclo ciclo) {
        executor.execute(() -> dao.insert(ciclo));
    }

    public LiveData<CicloConRegistrazioni> getCicloActual(){
        return dao.getCicloActualConRegistrazioni(getLocalId());
    }

    public LiveData<CicloConRegistrazioni> getCicloByIdConRegistrazioni(int cicloId){
        return dao.getCicloByIdConRegistrazioni(cicloId);
    }

    public static Date truncarFecha(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date prevDay(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return truncarFecha(calendar.getTime());
    }

    public static Date nextDay(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return truncarFecha(calendar.getTime());
    }

    public static int difDays(Date dataInizio, Date dataFine) {
        Date d1 = truncarFecha(dataInizio);
        Date d2 = truncarFecha(dataFine);
        long diferenciaMilisegundos = d2.getTime() - d1.getTime();
        return (int) (diferenciaMilisegundos / (1000 * 60 * 60 * 24));
    }
}