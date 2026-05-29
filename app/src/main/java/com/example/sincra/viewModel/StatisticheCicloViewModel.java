package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

public class StatisticheCicloViewModel extends AndroidViewModel {

    private final CicloRepository repo;

    private final MutableLiveData<Integer> cicloIdInput = new MutableLiveData<>();

    private final LiveData<CicloConRegistrazioni> cicloConRegistrazioni;

    public StatisticheCicloViewModel(@NonNull Application application){
        super(application);
        repo = new CicloRepository(application);

        cicloConRegistrazioni = Transformations.switchMap(cicloIdInput, id -> {
            return repo.getCicloByIdConRegistrazioni(id);
        });
    }

    public void setCicloId(int cicloId) {
        // Evitamos recargar la base de datos si es el mismo ID que ya está cargado
        if (cicloIdInput.getValue() != null && cicloIdInput.getValue() == cicloId) {
            return;
        }
        cicloIdInput.setValue(cicloId);
    }

    public LiveData<CicloConRegistrazioni> getCicloConRegistrazioni() {
        return cicloConRegistrazioni;
    }
}


