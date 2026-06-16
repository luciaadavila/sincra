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
        executor.execute(() -> dao.deleteRegistrazione(registrazione));
    }

    public void saveDay(Registrazione registro, List<ElementoCatalogo> elementos) {
        executor.execute(() -> {
            long userId = getLocalId();
            Date dataReg = CicloRepository.truncarFecha(registro.getData());

            // 1. Cerchiamo il ciclo a cui appartiene questa data (quello che è iniziato il o prima di dataReg)
            Ciclo c = cicloDao.getCicloPerDataSync(dataReg, userId);

            if (c == null) {
                // Se non c'è alcun ciclo precedente, ne creiamo uno iniziale per questa data
                c = new Ciclo(dataReg, null, 28, 5, userId);
                long id = cicloDao.insert(c);
                c.setCicloId((int) id);
            }

            // Colleghiamo la registrazione con il suo ciclo e ID utente (se necessario)
            registro.setCicloId(c.getCicloId());
            
            // 2. Calcoliamo SEMPRE il giorno del ciclo (1-based)
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
            Registrazione registrazione = dao.getRegistroByDateSync(dataTrunc, userId);

            if (registrazione == null) {
                // Se la registrazione non esiste, cerchiamo o creiamo il ciclo corrispondente
                Ciclo c = cicloDao.getCicloPerDataSync(dataTrunc, userId);

                if (c == null) {
                    c = new Ciclo(dataTrunc, null, 28, 5, userId);
                    long id = cicloDao.insert(c);
                    c.setCicloId((int) id);
                }

                int giornoCiclo = CicloRepository.difDays(c.getDataInizio(), dataTrunc);
                registrazione = new Registrazione(dataTrunc, false, giornoCiclo, c.getCicloId(), passi);
                dao.insert(registrazione);
            } else {
                // Se esiste già, aggiorniamo i passi
                int passiAttuali = registrazione.getPassi();
                registrazione.setPassi(Math.max(passiAttuali, passi));

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
