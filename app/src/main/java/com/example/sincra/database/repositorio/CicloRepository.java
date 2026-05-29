package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.PredictSettimana;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CicloRepository {

    private CicloDAO dao;
    private ExecutorService executor;

    private final Context context;

    public CicloRepository(Context context){
        this.context = context;
        dao = AppDatabase.getDatabase(context).cicloDAO();
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

    public void addOrDeletePeriodDay(Date date) {
        dao.addOrDeletePeriodDay(date);
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


}
