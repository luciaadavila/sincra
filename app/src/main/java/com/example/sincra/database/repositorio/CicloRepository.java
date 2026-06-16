package com.example.sincra.database.repositorio;

import android.content.Context;

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

    private final CicloDAO dao;
    private final RegistrazioneDAO daoRe;
    private final UserDAO daoUser;
    private final ExecutorService executor;
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

    public ExecutorService getExecutor() {
        return executor;
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
            Registrazione regActual = daoRe.getRegistroByDateSync(data, getLocalId());
            if (regActual == null) regActual = daoRe.getRegistroByDateSync(data, getLocalId());

            Registrazione regPrev = daoRe.getRegistroByDateSync(xDay(data, -1), getLocalId());
            if (regPrev == null) regPrev = daoRe.getRegistroByDateSync(xDay(data, -1), getLocalId());

            Registrazione regNext = daoRe.getRegistroByDateSync(xDay(data, 1), getLocalId());
            if (regNext == null) regNext = daoRe.getRegistroByDateSync(xDay(data, 1), getLocalId());

            if (regActual == null) {
                // Se non esiste, lo creiamo temporaneamente scollegato (cicloId = null)
                regActual = new Registrazione(data, false, 1, null, 0);
            }

            boolean isPeriodoActualmente = regActual.isPeriodo();

            // Invertiamo lo stato
            regActual.setPeriodo(!isPeriodoActualmente);
            long id = daoRe.insertOrUpdate(regActual);
            regActual.setRegistroId((int) id);

            if (!isPeriodoActualmente) {
                // CASO: AGGIUNGI CICLO
                handleAddPeriod(data, regActual, regPrev, regNext);
            } else {
                // CASSO: RIMUOVI CICLO
                handleRemovePeriod(data, regActual, regPrev, regNext);
            }
        });
    }


    private void handleAddPeriod(Date data, Registrazione regActual, Registrazione regPrev, Registrazione regNext) {
        boolean prevHuboRegla = (regPrev != null && regPrev.isPeriodo());
        boolean nextHuboRegla = (regNext != null && regNext.isPeriodo());

        // 1. MEZZO => copriamo un vuoto tra due giorni di ciclo
        if (prevHuboRegla && nextHuboRegla) {
            Ciclo cicloAnterior = dao.getByIdSync(regPrev.getCicloId());
            Ciclo cicloSiguiente = dao.getByIdSync(regNext.getCicloId());

            if (cicloAnterior != null && cicloSiguiente != null && cicloAnterior.getCicloId() != cicloSiguiente.getCicloId()) {
                // Uniamo il ciclo successivo all'interno del precedente
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + cicloSiguiente.getDurataPeriodo() + 1);
                cicloAnterior.setDataFine(cicloSiguiente.getDataFine());
                cicloAnterior.setDurataTotale(difDays(cicloAnterior.getDataInizio(), cicloAnterior.getDataFine()));
                dao.update(cicloAnterior);

                // Riassegnare tutte le registrazioni del ciclo successivo al ciclo precedente
                List<Registrazione> registrosSiguientes = daoRe.getRegistrosPosterioresSync(cicloSiguiente.getCicloId(), cicloSiguiente.getDataInizio(), getLocalId());
                if (registrosSiguientes != null) {
                    for (Registrazione r : registrosSiguientes) {
                        r.setCicloId(cicloAnterior.getCicloId());
                        daoRe.insertOrUpdate(r);
                    }
                }

                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
                dao.delete(cicloSiguiente);
                updatePeriodDays(cicloAnterior);

            } else if (cicloAnterior != null) {
                // appartenevano già allo stesso ciclo, aggiorniamo solo la durata
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + 1);
                dao.update(cicloAnterior);
                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
            }
        }
        // 2. AGGIUNGI ALL'INIZIO => selezionammo un giorno appena prima dell'inizio di un ciclo
        else if (!prevHuboRegla && nextHuboRegla) {
            Ciclo cicloSiguiente = dao.getByIdSync(regNext.getCicloId());
            if (cicloSiguiente != null) {
                cicloSiguiente.setDataInizio(data); // spostiamo l'inizio all'indietro
                cicloSiguiente.setDurataPeriodo(cicloSiguiente.getDurataPeriodo() + 1);
                if (cicloSiguiente.getDataFine() != null) {
                    cicloSiguiente.setDurataTotale(difDays(data, cicloSiguiente.getDataFine()));
                }
                dao.update(cicloSiguiente);

                regActual.setCicloId(cicloSiguiente.getCicloId());
                daoRe.insertOrUpdate(regActual);
                updatePeriodDays(cicloSiguiente);
            }
        }
        // 3. AGGIUNGI ALLA FINE => selezioniamo un giorno appena dopo la fine del ciclo
        else if (prevHuboRegla) {
            Ciclo cicloAnterior = dao.getByIdSync(regPrev.getCicloId());
            if (cicloAnterior != null) {
                cicloAnterior.setDurataPeriodo(cicloAnterior.getDurataPeriodo() + 1);
                dao.update(cicloAnterior);

                regActual.setCicloId(cicloAnterior.getCicloId());
                daoRe.insertOrUpdate(regActual);
            }
        }
        // 4. NUOVO CICLO => un giorno isolato
        else {
            Ciclo cicloAnteriorMasCercano = dao.getCicloAnteriorSync(data, getLocalId());
            Ciclo cicloPosterior = dao.getCicloPosteriorSync(data, getLocalId());

            Date dataFineNuevo = (cicloPosterior != null) ? xDay(cicloPosterior.getDataInizio(), -1) : null;

            Ciclo nuevoCiclo = new Ciclo(data, dataFineNuevo, 28, 1, getLocalId());
            if (dataFineNuevo != null) {
                nuevoCiclo.setDurataTotale(difDays(data, dataFineNuevo));
            }

            long nuevoCicloId = dao.insert(nuevoCiclo);
            regActual.setCicloId((int) nuevoCicloId);
            daoRe.insertOrUpdate(regActual);

            // Chiudere il ciclo precedente correttamente
            if (cicloAnteriorMasCercano != null && (cicloAnteriorMasCercano.getDataFine() == null || cicloAnteriorMasCercano.getDataFine().after(data))) {
                closePrevCiclo(cicloAnteriorMasCercano, xDay(data, -1));
            }

            Ciclo nuevoCargado = dao.getByIdSync((int) nuevoCicloId);
            if (nuevoCargado != null) updatePeriodDays(nuevoCargado);
        }
    }


    private void handleRemovePeriod(Date data, Registrazione regActual, Registrazione regPrev, Registrazione regNext) {
        Ciclo cicloActual = dao.getByIdSync(regActual.getCicloId());
        if (cicloActual == null) return;

        boolean prevHuboRegla = (regPrev != null && regPrev.isPeriodo() && regPrev.getCicloId() == cicloActual.getCicloId());
        boolean nextHuboRegla = (regNext != null && regNext.isPeriodo() && regNext.getCicloId() == cicloActual.getCicloId());

        cicloActual.setDurataPeriodo(Math.max(0, cicloActual.getDurataPeriodo() - 1));

        // 1. ELIMINARE: => Era l'unico giorno di ciclo
        if (cicloActual.getDurataPeriodo() == 0) {
            Ciclo cicloAnterior = dao.getCicloAnteriorSync(cicloActual.getDataInizio(), getLocalId());
            List<Registrazione> todosLosDelCiclo = daoRe.getRegistrosPosterioresSync(cicloActual.getCicloId(), cicloActual.getDataInizio(), getLocalId());

            if (cicloAnterior != null) {
                // Riassegnare i giorni al ciclo precedente
                if (todosLosDelCiclo != null) {
                    for (Registrazione r : todosLosDelCiclo) {
                        r.setCicloId(cicloAnterior.getCicloId());
                        r.setPeriodo(false);
                        daoRe.insertOrUpdate(r);
                    }
                }
                // Espandere il ciclo precedente
                Date nuevaDataFine = cicloActual.getDataFine();
                cicloAnterior.setDataFine(nuevaDataFine);
                if (nuevaDataFine != null) {
                    cicloAnterior.setDurataTotale(difDays(cicloAnterior.getDataInizio(), nuevaDataFine));
                }
                dao.update(cicloAnterior);
                dao.delete(cicloActual);
                updatePeriodDays(cicloAnterior);
            } else {
                // Non c'è un ciclo precedente, lasciare le registrazioni orfane o nel ciclo null
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
        // 2. RIMUOVERE IL PRIMO => spostare l'inizio in avanti
        else if (!prevHuboRegla && nextHuboRegla) {
            Date nuevaDataInizio = xDay(data, 1);
            cicloActual.setDataInizio(nuevaDataInizio);
            if (cicloActual.getDataFine() != null) {
                cicloActual.setDurataTotale(difDays(nuevaDataInizio, cicloActual.getDataFine()));
            }
            dao.update(cicloActual);

            // Il giorno che cancelliamo lo assegniamo al ciclo precedente se esiste
            Ciclo cicloAnterior = dao.getCicloAnteriorSync(data, getLocalId());
            if (cicloAnterior != null) {
                regActual.setCicloId(cicloAnterior.getCicloId());
                cicloAnterior.setDataFine(data);
                cicloAnterior.setDurataTotale(difDays(cicloAnterior.getDataInizio(), data));
                dao.update(cicloAnterior);
            } else {
                regActual.setCicloId(null);
                regActual.setGiornoCiclo(0);
            }
            daoRe.insertOrUpdate(regActual);
            updatePeriodDays(cicloActual);
        }
        // 3. RIMUOVERE L'ULTIMO => viene ridotta solo la durata del ciclo
        else if (prevHuboRegla && !nextHuboRegla) {
            dao.update(cicloActual);
        }
        // 4. DIVIDERE (Split) => abbiamo rimosso un giorno nel mezzo di un ciclo
        else if (prevHuboRegla) {
            dao.update(cicloActual);
        }
    }


    ///////////////////////////////////////////////////////
    public void closePrevCiclo(Ciclo ciclo, Date dataFine){
        Date fechaFinTruncada = truncarFecha(dataFine);
        ciclo.setDataFine(fechaFinTruncada);
        ciclo.setDurataTotale(difDays(ciclo.getDataInizio(), fechaFinTruncada));
        dao.update(ciclo);
        updatePeriodDays(ciclo);
        updateDurataMedia();
    }

    public void updateDurataMedia(){
        executor.execute(() -> {
            User user = daoUser.getByIdSync(getLocalId());
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
        Date hoy = truncarFecha(new Date());

        Date fechaFin;
        if (ciclo.getDataFine() != null) {
            fechaFin = truncarFecha(ciclo.getDataFine());
        } else {
            Date inicio = truncarFecha(ciclo.getDataInizio());

            if (inicio.after(hoy)) {
                fechaFin = inicio;
            } else {
                fechaFin = hoy;
            }
        }

        int i = 1;
        int countPeriodo = 0;

        while (!dataEv.after(fechaFin)) {
            Registrazione reg = daoRe.getRegistroByDateSync(dataEv, getLocalId());

            if (reg == null) {
                reg = new Registrazione(dataEv, false, i, ciclo.getCicloId(), 0);
            }

            if (reg.getCicloId() == null || reg.getCicloId().equals(ciclo.getCicloId())) {
                reg.setCicloId(ciclo.getCicloId());
                reg.setGiornoCiclo(i);
                daoRe.insertOrUpdate(reg);

                if (reg.isPeriodo()) {
                    countPeriodo++;
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
            dataEv = truncarFecha(cal.getTime());
            i++;
        }

        if (ciclo.getDurataPeriodo() != countPeriodo) {
            ciclo.setDurataPeriodo(countPeriodo);
            dao.update(ciclo);
        }

    }

    private int calcoloMediaPeriodoUltimi4Cicli(){
        User user = daoUser.getByIdSync(getLocalId());
        List<Ciclo> ultimi = dao.getLastFourCicliSync(getLocalId());
        if (ultimi == null || ultimi.isEmpty()){
            return 0;
        }
        int sumDays = user.getDurataMediaPeriodo();
        for (Ciclo c: ultimi){
            sumDays += c.getDurataPeriodo();
        }


        return Math.round( (float) sumDays / (ultimi.size()+1));
    }

    private int calcoloMediaCicloUltimi4Cicli(){
        User user = daoUser.getByIdSync(getLocalId());
        List<Ciclo> ultimi = dao.getLastFourCicliSync(getLocalId());
        if (ultimi == null || ultimi.isEmpty()){
            return 0;
        }
        int sumDays = user.getDurataMediaCiclo();
        for (Ciclo c: ultimi){
            sumDays += c.getDurataTotale();
        }
        return Math.round((float) sumDays / (ultimi.size()+1));
    }

    private List<Date> calcoloGiorniProbabileSync(Date inizioUltimoCiclo, User user, int numeroCicli) {
        List<Date> giorni = new ArrayList<>();

        if (inizioUltimoCiclo == null || user == null) {
            return giorni;
        }

        int durataMediaCiclo = user.getDurataMediaCiclo();
        int durataMediaPeriodo = user.getDurataMediaPeriodo();

        if (durataMediaCiclo <= 0) durataMediaCiclo = 28;
        if (durataMediaPeriodo <= 0) durataMediaPeriodo = 5;

        Date iterCiclo = inizioUltimoCiclo;

        for (int ciclo = 0; ciclo < numeroCicli; ciclo++) {
            iterCiclo = xDay(iterCiclo, durataMediaCiclo);

            Date iter = iterCiclo;

            for (int i = 0; i < durataMediaPeriodo; i++) {
                giorni.add(iter);
                iter = xDay(iter, 1);
            }
        }
        return giorni;
    }

    public void calcoloGiorniProbabile(Date inizioUltimoCiclo, PredictionsCallback callback) {
        executor.execute(() -> {
            User user = daoUser.getByIdSync(getLocalId());
            List<Date> giorni = calcoloGiorniProbabileSync(
                    inizioUltimoCiclo,
                    user,
                    3
            );
            callback.onResult(giorni);
        });
    }



    public void generatePredictions(PredictionCallback callback) {
        executor.execute(() -> {
            User user = daoUser.getByIdSync(getLocalId());
            Ciclo ciclo = dao.getCurrentCicloSync(getLocalId());
            List<PredictSettimana> risultato = new ArrayList<>();

            if (user == null || ciclo == null) {
                callback.onPredictionsGenerated(risultato);
                return;
            }

            List<Date> giorni = calcoloGiorniProbabileSync(ciclo.getDataInizio(), user, 4);
            if (giorni.isEmpty()) {
                callback.onPredictionsGenerated(risultato);
                return;
            }

            List<Date>  inizi = obtenerIniciosPeriodos(giorni);

            for (int i=0; i<inizi.size(); i++){
                Date inizioPeriodo = inizi.get(i);
                Date inizioCalendario = xDay(inizioPeriodo, -7);

                risultato.add(creaSettimanaPredizione( "Ciclo " + (i+1), inizioCalendario, giorni));
            }
            callback.onPredictionsGenerated(risultato);
        });
    }

    // crea una settimana (21 giorni) verificando se è un giorno probabile o no
    private PredictSettimana creaSettimanaPredizione(String titolo, Date inizioSettimana, List<Date> giorniProbabili){
        List<Boolean> periodo = new ArrayList<>();
        List<Integer> numeri = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(inizioSettimana);


        for (int i=0; i<21; i++){
            Date giornoAttuale = truncarFecha(cal.getTime());
            Calendar calGiorno = Calendar.getInstance();
            calGiorno.setTime(giornoAttuale);

            numeri.add(calGiorno.get(Calendar.DAY_OF_MONTH));

            boolean isPeriodo = contieneData(giorniProbabili, giornoAttuale);
            periodo.add(isPeriodo);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        Date fineSettimana = xDay(inizioSettimana, 20);
        String rango = titolo + "\n" + formatRango(inizioSettimana, fineSettimana);

        return new PredictSettimana(rango, periodo, numeri);
    }

    // vogliamo ottenere l'inizio di ogni ciclo per creare il mini calendario a partire da esso
    private List<Date> obtenerIniciosPeriodos(List<Date> giorniProbabili) {
        List<Date> inicios = new ArrayList<>();

        if (giorniProbabili == null || giorniProbabili.isEmpty()) {
            return inicios;
        }

        inicios.add(truncarFecha(giorniProbabili.get(0)));

        for (int i = 1; i < giorniProbabili.size(); i++) {
            Date anterior = truncarFecha(giorniProbabili.get(i - 1));
            Date actual = truncarFecha(giorniProbabili.get(i));

            Date diaDespuesAnterior = xDay(anterior, 1);

            if (!actual.equals(diaDespuesAnterior)) {
                inicios.add(actual);
            }
        }

        return inicios;
    }

    // per sapere se la data è un giorno probabile o no
    private boolean contieneData(List<Date> fechas, Date fechaBuscada) {
        Date buscada = truncarFecha(fechaBuscada);
        for (Date fecha : fechas) {
            if (truncarFecha(fecha).equals(buscada)) {
                return true;
            }
        }
        return false;
    }

    // per impostare bene il testo dell'intervallo
    private String formatRango(Date inicio, Date fin) {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault());

        return sdf.format(inicio) + " - " + sdf.format(fin);
    }

    public interface PredictionCallback {
        void onPredictionsGenerated(List<PredictSettimana> predictions);
    }



    public interface PredictionsCallback {
        void onResult(List<Date> fechasProbables);
    }


    /////////////////////////////////////////////////
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
        // +1 perchè un ciclo dal 1 al 28 duri 28 giorni, non 27.
        return (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
    }



    public LiveData<CicloConRegistrazioni> getCicloActual(){ return dao.getCicloActualConRegistrazioni(getLocalId()); }
    public LiveData<CicloConRegistrazioni> getCicloByIdConRegistrazioni(int cicloId){ return dao.getCicloByIdConRegistrazioni(cicloId); }

    public Ciclo getCicloPerData(Date data) {
        return dao.getCicloPerDataSync(truncarFecha(data), getLocalId());
    }

    public LiveData<List<Date>> getFechasConPeriodo() {
        return daoRe.getFechasConPeriodoByUserId(getLocalId());
    }
}
