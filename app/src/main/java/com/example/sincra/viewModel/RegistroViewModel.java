package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class RegistroViewModel extends AndroidViewModel {

    private final RegistrazioneRepository repo;
    private final CicloRepository cicloRepo;
    private final LiveData<List<RegistrazioneConElementi>> registroList;
    private final LiveData<List<CicloConRegistrazioni>> cicliList;

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        repo = new RegistrazioneRepository(application);
        cicloRepo = new CicloRepository(application);

        this.registroList = repo.getAll();
        this.cicliList = cicloRepo.getCicliConRegistrazioni();
    }

    public LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni() {
        return cicliList;
    }


    public boolean deleteRegistrazione(Registrazione registrazione){
        if (registrazione == null) return false;
        if (registrazione.isPeriodo()){
            return false;
        }
        repo.deleteRegistrazione(registrazione);
        return true;
    }

    public LiveData<List<RegistrazioneConElementi>> getRegistri() {
        return registroList;
    }

    public LiveData<RegistrazioneConElementi> getByDate(String date){
        return repo.getByDate(date);
    }
}
