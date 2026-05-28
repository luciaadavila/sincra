package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.PredictSettimana;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CicloRepository {
    private CicloDAO dao;
    private ExecutorService executor;
    private int userId;

    public CicloRepository(Context context){
        dao = AppDatabase.getDatabase(context).cicloDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    /*
    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return ""; // O manejar el error si no hay un usuario logueado
    }*/

    public LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni() {
        return dao.getCicliConRegistrazioni(userId);
    }

    public LiveData<List<Ciclo>> getHistorialCicli(){
        return dao.getHistorialCicli(userId);
    }

    public interface PredictionCallback {
        void onPredictionsGenerated(List<PredictSettimana> predictions);
    }

    public void generatePredictions(PredictionCallback callback) {
        executor.execute(() -> {
            // 1. Obtenemos el historial de forma síncrona para el cálculo (Room permite llamadas directas en hilos background)
            List<Ciclo> historial = dao.getHistorialCicliSync(userId);

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


}
