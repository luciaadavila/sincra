package com.example.sincra.database.repositorio;

import android.content.Context;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.model.ElementoCatalogo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.callback.Callback;

public class ElementoCatalogoRepository {
    public final ElementoCatalogoDAO dao;
    public final ExecutorService executor;

    public ElementoCatalogoRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.elementoCatalogoDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(ElementoCatalogo e){
        executor.execute(() -> dao.insert(e));
    }

    public void delete(ElementoCatalogo elementoCatalogo){
        executor.execute(() -> dao.delete(elementoCatalogo));
    }

    public void getByType(String tipo, Callback callback){
        executor.execute(() -> {
            List<ElementoCatalogo> result = dao.getByType(tipo);
            callback.onResult(result);
        });
    }

    public interface Callback {
        void onResult(List<ElementoCatalogo> result);
    }
}
