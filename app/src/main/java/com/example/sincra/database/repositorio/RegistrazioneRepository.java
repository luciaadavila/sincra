package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrazioneRepository {
    private final RegistrazioneDAO dao;
    private final ElementoCatalogoDAO catalogoDAO;
    private final ExecutorService executor;

    public RegistrazioneRepository(Context context){
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.registrazioneDAO();
        catalogoDAO = db.elementoCatalogoDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    private String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }

    public LiveData<List<ElementoCatalogo>> getAllElementosByUsuario(String tipo) {
        return catalogoDAO.getElementosByUsuarioAndTipo(getUid(), tipo);
    }
    public LiveData<List<RegistrazioneConElementi>> getAll() {
        return dao.getAllByUserId(getUid());
    }

    public LiveData<RegistrazioneConElementi> getByDate(String date) {
        return dao.getByDateAndUser(date, getUid());
    }

    public void saveDay(Registrazione registro, List<ElementoCatalogo> elementos) {
        executor.execute(() -> {
            // Extraemos solo los IDs para pasárselos al método por defecto del DAO
            List<Integer> ids = new ArrayList<>();
            for (ElementoCatalogo e : elementos) {
                ids.add(e.getElementoId());
            }
            dao.insertRegistroCompleto(registro, ids);
        });
    }

}
