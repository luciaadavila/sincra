package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class DetailDayViewModel extends AndroidViewModel {

    private final RegistrazioneRepository repo;

    private final MutableLiveData<RegistrazioneConElementi> registro =
            new MutableLiveData<>();

    public DetailDayViewModel(@NonNull Application application) {
        super(application);
        repo = new RegistrazioneRepository(application);
    }

    public MutableLiveData<RegistrazioneConElementi> getRegistro() {
        return registro;
    }

    public void loadByDate(String date) {
        repo.getByDate(date, data -> registro.postValue(data));
    }

    public void save(Registrazione r, List<ElementoCatalogo> selected) {
        repo.saveDay(r, selected);
    }
}