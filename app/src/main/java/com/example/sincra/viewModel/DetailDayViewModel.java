package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public class DetailDayViewModel extends AndroidViewModel {

    private final RegistrazioneRepository repo;
    private final CicloRepository cicloRepo;
    // para la fecha seleccionada
    private final MutableLiveData<String> dateInput = new MutableLiveData<>();
    private final LiveData<RegistrazioneConElementi> registro;
    private final LiveData<List<ElementoCatalogo>> allMoods;
    private final LiveData<List<ElementoCatalogo>> allSymptoms;
    private final LiveData<CicloConRegistrazioni> cicloActual;


    public DetailDayViewModel(@NonNull Application application) {
        super(application);
        repo = new RegistrazioneRepository(application);
        cicloRepo = new CicloRepository(application);
        // cada vez que se cambia la fecha con setDate() se relanza la consulta de forma reactiva
        registro = Transformations.switchMap(dateInput, repo::getByDate);

        allMoods = repo.getAllElementosByUsuario("mood");
        allSymptoms = repo.getAllElementosByUsuario("symptom");
        cicloActual = cicloRepo.getCicloActual();
    }

    public LiveData<CicloConRegistrazioni> getCicloActual() {
        return cicloActual;
    }

    public LiveData<RegistrazioneConElementi> getRegistro() {
        return registro;
    }

    public LiveData<List<ElementoCatalogo>> getAllMoods() {
        return allMoods;
    }

    public LiveData<List<ElementoCatalogo>> getAllSymptoms() {
        return allSymptoms;
    }

    public void setDate(String date) {
        dateInput.setValue(date);
    }

    public void save(Registrazione r, List<ElementoCatalogo> selected) {
        repo.saveDay(r, selected);
    }

}