package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class RegistroViewModel extends AndroidViewModel {
    private final RegistrazioneRepository repo;
    private final MutableLiveData<List<RegistrazioneConElementi>> registro = new MutableLiveData<>();

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        repo = new RegistrazioneRepository(application);
    }

    public MutableLiveData<List<RegistrazioneConElementi>> getRegistro() {
        return registro;
    }

    public void loadAll() {
        repo.getAll(data -> registro.postValue(data));
    }


}
