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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrazioneRepository {
    private final RegistrazioneDAO dao;
    private final ElementoCatalogoDAO catalogoDAO;
    private final ExecutorService executor;

    private final Context context;

    public RegistrazioneRepository(Context context){
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.registrazioneDAO();
        catalogoDAO = db.elementoCatalogoDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    private long getLocalId() {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getLong("local_user_id", -1L);
    }

    public LiveData<List<ElementoCatalogo>> getAllElementosByUsuario(String tipo) {
        return catalogoDAO.getElementosByUsuarioAndTipo(getLocalId(), tipo);
    }
    public LiveData<List<RegistrazioneConElementi>> getAll() {
        return dao.getAllByUserId(getLocalId());
    }

    public LiveData<RegistrazioneConElementi> getByDate(String date) {
        Date data = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            data = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dao.getByDateAndUser(data, getLocalId());
    }

    public void saveDay(Registrazione registro, List<ElementoCatalogo> elementos) {
        executor.execute(() -> {
            // Si el registro no tiene un ciclo asignado (es nuevo), buscamos el ciclo actual
            if (registro.getCicloId() == 0) {
                com.example.sincra.database.dao.CicloDAO cicloDao = AppDatabase.getDatabase(context).cicloDAO();
                com.example.sincra.model.Ciclo current = cicloDao.getCurrentCiclo(getLocalId());
                if (current != null) {
                    registro.setCicloId(current.getCicloId());
                } else {
                    // Si no hay ciclo, creamos uno base para evitar el crash por Foreign Key
                    com.example.sincra.model.Ciclo nuevoCiclo = new com.example.sincra.model.Ciclo(new Date(), null, 28, 5, getLocalId());
                    long id = cicloDao.insert(nuevoCiclo);
                    registro.setCicloId((int) id);
                }
            }

            // Extraemos solo los IDs para pasárselos al metodo por defecto del DAO
            List<Integer> ids = new ArrayList<>();
            for (ElementoCatalogo e : elementos) {
                ids.add(e.getElementoId());
            }
            dao.insertRegistroCompleto(registro, ids);
        });
    }

}
