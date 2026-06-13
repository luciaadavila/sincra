package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.Ciclo;

import java.util.List;

public class HistorialViewModel extends AndroidViewModel {
    private final LiveData<List<Ciclo>> cicli;


    public HistorialViewModel(@NonNull Application application) {
        super(application);
        CicloRepository repo = new CicloRepository(application);
        this.cicli = repo.getHistorialCicli();
    }

    public LiveData<List<Ciclo>> getHistorialCicli(){
        return cicli;
    }

}
