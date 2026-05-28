package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class HistorialViewModel extends AndroidViewModel {
    private final CicloRepository repo;
    private final LiveData<List<Ciclo>> cicli;

    public HistorialViewModel(@NonNull Application application) {
        super(application);
        repo = new CicloRepository(application);
        this.cicli = repo.getHistorialCicli();
    }

    public LiveData<List<Ciclo>> getHistorialCicli(){
        return cicli;
    }

}
