package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private final LiveData<CicloConRegistrazioni> cicloActual;
    private final LiveData<List<String>> diasDeRegla;
    private final MutableLiveData<List<Date>> listaFechas = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final CicloRepository repo;

    public HomeViewModel(@NonNull Application application){
        super(application);
        repo = new CicloRepository(application);
        this.cicloActual = repo.getCicloActual();

        // Si no hay ciclo, creamos uno por defecto (para evitar crash en DetailDay)
        this.cicloActual.observeForever(cicloConRegistrazioni -> {
            if (cicloConRegistrazioni == null) {
                long localId = repo.getLocalId();
                if (localId != -1) {
                    Ciclo nuevoCiclo = new Ciclo(new Date(), null, 28, 5, localId);
                    repo.insert(nuevoCiclo);
                }
            }
        });
        
        this.diasDeRegla = Transformations.map(cicloActual, cicloConRegistrazioni -> {
            List<String> dias = new ArrayList<>();
            if (cicloConRegistrazioni != null && cicloConRegistrazioni.getRegistrazioni() != null) {
                for (Registrazione r : cicloConRegistrazioni.getRegistrazioni()) {
                    if (r.isPeriodo()) {
                        dias.add(dateFormat.format(r.getDate()));
                    }
                }
            }
            return dias;
        });

        generateFechas();
    }

    private void generateFechas() {
        List<Date> fechas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -15);
        for (int i = 0; i < 31; i++) {
            fechas.add(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        listaFechas.setValue(fechas);
    }

    public LiveData<CicloConRegistrazioni> getCicloActual() {
        return cicloActual;
    }

    public LiveData<List<String>> getDiasDeRegla() {
        return diasDeRegla;
    }

    public LiveData<List<Date>> getListaFechas() {
        return listaFechas;
    }
    
    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public void addOrDeletePeriodDay(Date date){
        repo.addOrDeletePeriodDay(date);
    }
}
