package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.Date;
import java.util.List;

public class RegistroViewModel extends AndroidViewModel {

    private final RegistrazioneRepository repo;
    private final LiveData<List<RegistrazioneConElementi>> registroList;

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        repo = new RegistrazioneRepository(application);
        this.registroList = repo.getAll();
    }

    public LiveData<List<RegistrazioneConElementi>> getRegistri() {
        return registroList;
    }

    public LiveData<RegistrazioneConElementi> getByDate(String date){
        return repo.getByDate(date);
    }
}
