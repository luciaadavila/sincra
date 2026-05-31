package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
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
    private final MutableLiveData<List<String>> diasProbables = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final CicloRepository repo;

    public HomeViewModel(@NonNull Application application){
        super(application);
        repo = new CicloRepository(application);

        // Inicializamos los LiveData básicos
        this.cicloActual = repo.getCicloActual();

        // Transformación de los días de regla para compararlos fácilmente en el Adapter
        this.diasDeRegla = Transformations.map(repo.getFechasConPeriodo(), lista -> {
            List<String> diasStr = new ArrayList<>();
            if (lista != null) {
                for (Date d : lista) {
                    diasStr.add(dateFormat.format(d));
                }
            }
            return diasStr;
        });

        generateFechas();
    }

    /**
     * Calcula los días probables llamando al repositorio.
     * Esta función debe ser llamada desde el Fragment cuando el ciclo actual esté disponible.
     */
    public void calcoloPredict(Date inizioCiclo){
        repo.calcoloGiorniProbabile(inizioCiclo, datas -> {
            List<String> giorniP = new ArrayList<>();
            for (Date d : datas) {
                giorniP.add(dateFormat.format(d));
            }
            // Usamos postValue porque estamos en un hilo secundario (background thread)
            diasProbables.postValue(giorniP);
        });
    }

    private void generateFechas() {
        List<Date> fechas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        // Truncamos la fecha de hoy para empezar desde las 00:00
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, -15);
        for (int i = 0; i < 31; i++) {
            fechas.add(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        listaFechas.setValue(fechas);
    }

    // Getters para que el Fragment pueda observar
    public LiveData<CicloConRegistrazioni> getCicloActual() { return cicloActual; }
    public LiveData<List<String>> getDiasDeRegla() { return diasDeRegla; }
    public LiveData<List<String>> getDiasProbables() { return diasProbables; }
    public LiveData<List<Date>> getListaFechas() { return listaFechas; }

    public String formatDate(Date date) { return dateFormat.format(date); }

    public void addOrDeletePeriodDay(Date date){
        repo.addOrDeletePeriodDay(date);
    }
}