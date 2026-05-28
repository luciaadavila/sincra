package com.example.sincra.database.repositorio;

import android.content.Context;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrazioneRepository {
    public final RegistrazioneDAO dao;
    public final ExecutorService executor;

    public RegistrazioneRepository(Context context){
        dao = AppDatabase.getDatabase(context).registrazioneDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    public void getByDate(String date, Callback callback){
        executor.execute(() -> {
            RegistrazioneConElementi result = dao.getByDate(date);
            callback.onResult(result);
        });
    }

    public void saveDay(Registrazione registro, List<ElementoCatalogo> elementos){
        executor.execute(() -> {
            // insertar registrazione
            long registroId = dao.insert(registro);

            // crear las relaciones n:n registro_catalogo
            for (ElementoCatalogo e: elementos){
                RegistroCatalogoRel rel = new RegistroCatalogoRel((int) registroId, e.getElementoId());
                dao.insertRel(rel);
            }
        });
    }

    public void getAll(ListCallback callback){
        executor.execute(() -> {
            List<RegistrazioneConElementi> result = dao.getAll();
            callback.onResult(result);
        });
    }

    public interface Callback {
        void onResult(RegistrazioneConElementi result);
    }

    public interface ListCallback {
        void onResult(List<RegistrazioneConElementi> result);
    }
}
