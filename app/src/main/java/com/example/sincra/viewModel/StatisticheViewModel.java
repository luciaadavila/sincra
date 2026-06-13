package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.utils.StatisticheCalculator;

import java.util.List;

public class StatisticheViewModel extends AndroidViewModel {

    private final MediatorLiveData<StatisticheCalculator.StatisticheResult> statistiche = new MediatorLiveData<>();

    private List<CicloConRegistrazioni> ultimiCicli;
    private List<RegistrazioneConElementi> ultimeRegistrazioniConElementi;

    public StatisticheViewModel(@NonNull Application application){
        super(application);

        CicloRepository cicloRepository = new CicloRepository(application);
        RegistrazioneRepository registrazioneRepository = new RegistrazioneRepository(application);

        LiveData<List<CicloConRegistrazioni>> cicliSource = cicloRepository.getCicliConRegistrazioni();
        LiveData<List<RegistrazioneConElementi>> registrazioniSource = registrazioneRepository.getAll();

        statistiche.addSource(cicliSource, cicli -> {
            ultimiCicli = cicli;
            ricalcolaStatistiche();
        });

        statistiche.addSource(registrazioniSource, registrazioni -> {
            ultimeRegistrazioniConElementi = registrazioni;
            ricalcolaStatistiche();
        });
    }

    private void ricalcolaStatistiche(){
        if (ultimiCicli == null || ultimeRegistrazioniConElementi == null){
            return;
        }

        StatisticheCalculator.StatisticheResult result = StatisticheCalculator.calcola(ultimiCicli, ultimeRegistrazioniConElementi);
        statistiche.setValue(result);
    }

    public LiveData<StatisticheCalculator.StatisticheResult> getStatistiche(){
        return statistiche;
    }
}
