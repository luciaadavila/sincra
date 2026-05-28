package com.example.sincra.database.repositorio;

import android.content.Context;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CicloRepository {
    public final CicloDAO dao;
    public final ExecutorService executor;

    public CicloRepository(Context context){
        dao = AppDatabase.getDatabase(context).cicloDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    public void getAll(ListCallback callback){
        executor.execute(() -> {
            List<CicloConRegistrazioni> result = dao.getCicliConRegistrazioni();
            callback.onResult(result);
        });
    }

    public interface ListCallback {
        void onResult(List<CicloConRegistrazioni> result);
    }
}
