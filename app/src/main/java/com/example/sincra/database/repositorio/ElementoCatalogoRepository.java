package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.callback.Callback;

public class ElementoCatalogoRepository {
    private final ElementoCatalogoDAO dao;
    private final ExecutorService executor;

    private final Context context;

    public ElementoCatalogoRepository(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.elementoCatalogoDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    private long getLocalId() {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getLong("local_user_id", -1L);
    }

    public void insert(ElementoCatalogo e){
        executor.execute(() -> dao.insert(e));
    }

    public void delete(ElementoCatalogo elementoCatalogo){
        executor.execute(() -> dao.delete(elementoCatalogo));
    }

    public LiveData<List<ElementoCatalogo>> getByType(String tipo){
        return dao.getElementosByUsuarioAndTipo(getLocalId(), tipo);
    }

}
