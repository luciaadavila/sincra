package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.PredictSettimana;

import java.util.List;

public class PredictViewModel extends AndroidViewModel {

    private final CicloRepository repo;
    private final MutableLiveData<List<PredictSettimana>> proxCicli = new MutableLiveData<>();


    public PredictViewModel(@NonNull Application application) {
        super(application);
        repo = new CicloRepository(application);
    }

    public LiveData<List<PredictSettimana>> getProxCicli() {
        return proxCicli;
    }

    public void loadPredictions() {
        if (proxCicli.getValue() != null){
            return;
        }
        repo.generatePredictions(proxCicli::postValue);
    }
}
