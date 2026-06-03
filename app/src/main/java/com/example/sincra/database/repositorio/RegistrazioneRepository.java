package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrazioneRepository {
    private final RegistrazioneDAO dao;
    private final ElementoCatalogoDAO catalogoDAO;
    private final CicloDAO cicloDao;
    private final ExecutorService executor;

    private final Context context;

    public RegistrazioneRepository(Context context){
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.registrazioneDAO();
        catalogoDAO = db.elementoCatalogoDAO();
        cicloDao = db.cicloDAO();
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

    public LiveData<List<RegistrazioneConElementi>> getRegistrazioniConElementiByCiclo(int cicloId){
        return dao.getRegistrazioniConElementiByCicloId(cicloId);
    }

    public LiveData<RegistrazioneConElementi> getByDate(String date) {
        Date data = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            data = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dao.getByDateAndUser(data, getLocalId());
    }

    public void deleteRegistrazione(Registrazione registrazione) {
        executor.execute(() -> {
            dao.deleteRegistrazione(registrazione);
        });
    }

    public void saveDay(Registrazione registro, List<ElementoCatalogo> elementos) {
        executor.execute(() -> {
            long userId = getLocalId();
            Date dataReg = CicloRepository.truncarFecha(registro.getData());

            // 1. Buscamos el ciclo al que pertenece esta fecha (el que empezó en o antes de dataReg)
            Ciclo c = cicloDao.getCicloPerDataSync(dataReg, userId);

            if (c == null) {
                // Si no hay ningún ciclo previo, creamos uno inicial para esta fecha
                c = new Ciclo(dataReg, null, 28, 5, userId);
                long id = cicloDao.insert(c);
                c.setCicloId((int) id);
            }

            // Vinculamos la registración con su ciclo e ID de usuario (si fuera necesario)
            registro.setCicloId(c.getCicloId());
            
            // 2. Calculamos SIEMPRE el día del ciclo (1-based)
            int giornoCalcolato = CicloRepository.difDays(c.getDataInizio(), dataReg);
            registro.setGiornoCiclo(giornoCalcolato);

            List<Integer> elementoIds = new ArrayList<>();
            if (elementos != null) {
                for (ElementoCatalogo e : elementos) {
                    elementoIds.add(e.getElementoId());
                }
            }
            dao.insertRegistroCompleto(registro, elementoIds);
        });
    }

    public void savePassiOggi(Date date, int passi) {
        if (date == null || passi < 0) {
            return;
        }

        executor.execute(() -> {
            Date dataTrunc = CicloRepository.truncarFecha(date);
            long userId = getLocalId();
            Registrazione registrazione = dao.getRegistroByDate(dataTrunc, userId);

            if (registrazione == null) {
                // Si no existe el registro, buscamos o creamos el ciclo correspondiente
                Ciclo c = cicloDao.getCicloPerDataSync(dataTrunc, userId);

                if (c == null) {
                    c = new Ciclo(dataTrunc, null, 28, 5, userId);
                    long id = cicloDao.insert(c);
                    c.setCicloId((int) id);
                }

                int giornoCiclo = CicloRepository.difDays(c.getDataInizio(), dataTrunc);
                registrazione = new Registrazione(dataTrunc, false, false, giornoCiclo, null, c.getCicloId(), passi);
                dao.insert(registrazione);
            } else {
                // Si ya existe, actualizamos los pasos
                int pasosActuales = registrazione.getPasos();
                registrazione.setPasos(Math.max(pasosActuales, passi));

                // Por seguridad, si los datos del ciclo son inválidos, los recalculamos
                if (registrazione.getGiornoCiclo() <= 0 || registrazione.getCicloId() == null) {
                    Ciclo c = cicloDao.getCicloPerDataSync(dataTrunc, userId);
                    if (c != null) {
                        registrazione.setCicloId(c.getCicloId());
                        registrazione.setGiornoCiclo(CicloRepository.difDays(c.getDataInizio(), dataTrunc));
                    }
                }
                dao.update(registrazione);
            }
        });
    }
}
