package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class HistorialViewModel extends AndroidViewModel {
    private final CicloRepository repo;
    private final MutableLiveData<List<CicloConRegistrazioni>> cicli = new MutableLiveData<>();

    public HistorialViewModel(@NonNull Application application) {
        super(application);
        repo = new CicloRepository(application);
    }

    public MutableLiveData<List<CicloConRegistrazioni>> getCicli() {
        return cicli;
    }

    public void loadAll() {
        repo.getAll(data -> cicli.postValue(data));
    }

}
