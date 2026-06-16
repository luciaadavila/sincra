package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class StatisticheCicloViewModel extends AndroidViewModel {

    private final CicloRepository repoCiclo;
    private final RegistrazioneRepository repoReg;
    private final MutableLiveData<Integer> cicloIdInput = new MutableLiveData<>();
    private final LiveData<CicloConRegistrazioni> cicloConRegistrazioni;
    private final LiveData<List<RegistrazioneConElementi>> registrazioniConElementi;

    public StatisticheCicloViewModel(@NonNull Application application){
        super(application);
        repoCiclo = new CicloRepository(application);
        repoReg = new RegistrazioneRepository(application);

        cicloConRegistrazioni = Transformations.switchMap(cicloIdInput, repoCiclo::getCicloByIdConRegistrazioni
        );

        registrazioniConElementi = Transformations.switchMap(cicloIdInput, repoReg::getRegistrazioniConElementiByCiclo
        );
    }

    public void setCicloId(int cicloId) {
        // evitiamo di ricaricare il database se è lo stesso ID già caricato
        if (cicloIdInput.getValue() != null && cicloIdInput.getValue() == cicloId) {
            return;
        }
        cicloIdInput.setValue(cicloId);
    }

    public LiveData<CicloConRegistrazioni> getCicloConRegistrazioni() {
        return cicloConRegistrazioni;
    }

    public LiveData<List<RegistrazioneConElementi>> getRegistrazioniConElementi(){
        return registrazioniConElementi;
    }
}


