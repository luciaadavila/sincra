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

    public void addOrDeletePeriodDay(Date data) {
        executor.execute(() -> {
            Registrazione registrazioneDate = daoRe.getRegistroByDate(data, getLocalId());
            Registrazione registrazionePrevDate = daoRe.getRegistroByDate(prevDay(data), getLocalId());
            Ciclo currentCiclo = dao.getCurrentCiclo(getLocalId());

            // si todavía no había una registración, significa que el día no era de regla
            // creamos registrazione y la insertamos
            if (registrazioneDate == null){
                registrazioneDate = new Registrazione(data, false, false, 1, null, currentCiclo.getCicloId(), 0);
            }
            boolean isPeriodDay = registrazioneDate.isPeriodo();

            if (isPeriodDay){
                registrazioneDate.setPeriodo(false);
                daoRe.insertOrUpdate(registrazioneDate);

                // si quitamos una regla intermedia temeos que cambiar lógica
                return;
            }

            boolean wasPeriodDay = (registrazionePrevDate != null && registrazionePrevDate.isPeriodo());
            if (!wasPeriodDay){ // si el día anterior no había regla y ahora sí -> empieza nuevo ciclo
                if (currentCiclo != null && currentCiclo.getDataFine() == null){
                    closePrevCiclo(currentCiclo, data);
                }

                Ciclo nuevoCiclo = new Ciclo(data, null, 28, 5, getLocalId());
                long nuevoCicloId = dao.insert(nuevoCiclo);
                registrazioneDate.setCicloId((int) nuevoCicloId);
                daoRe.insertOrUpdate(registrazioneDate);

            } else {
                if (currentCiclo != null){
                    registrazioneDate.setCicloId(currentCiclo.getCicloId());
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
        while (dataEv.after(dataFine)){
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

    public interface PredictionCallback {
        void onPredictionsGenerated(List<PredictSettimana> predictions);
    }

    public void generatePredictions(PredictionCallback callback) {
        executor.execute(() -> {
            // 1. Obtenemos el historial de forma síncrona para el cálculo (Room permite llamadas directas en hilos background)
            List<Ciclo> historial = dao.getHistorialCicliSync(getLocalId());

            List<PredictSettimana> resultadoPredicciones = new ArrayList<>();

            if (historial != null && !historial.isEmpty()) {
                // TODO: Aquí irá tu lógica/algoritmo matemático de predicción real.
                // Ejemplo ficticio para que no vuelva vacío:
                // resultadoPredicciones = miAlgoritmo.calcular(historial);
            }

            // 2. Devolvemos el resultado al ViewModel
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

    public static int difDays(Date dataInizio, Date dataFine) {
        Date d1 = truncarFecha(dataInizio);
        Date d2 = truncarFecha(dataFine);
        long diferenciaMilisegundos = d2.getTime() - d1.getTime();
        // Añadimos +1 si quieres contar ambos días inclusive, o déjalo así para medir noches/intervalos.
        return (int) (diferenciaMilisegundos / (1000 * 60 * 60 * 24));
    }


}
