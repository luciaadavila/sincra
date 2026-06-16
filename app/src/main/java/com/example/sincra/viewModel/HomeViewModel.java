package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.RegistrazioneRepository;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.utils.FaseCiclo;
import com.example.sincra.utils.FaseCicloUtils;
import com.example.sincra.utils.StatisticheCalculator;

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
    private final MutableLiveData<FaseCiclo> faseSeleccionada = new MutableLiveData<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final CicloRepository repo;
    private final RegistrazioneRepository repoReg;

    private final MutableLiveData<Date> dataSelezionata = new MutableLiveData<>();

    private final MediatorLiveData<StatisticheCalculator.StatisticheFase> statistiche = new MediatorLiveData<>();

    private List<CicloConRegistrazioni> ultimiCicli;
    private List<RegistrazioneConElementi> ultimeRegistrazioniConElementi;
    private FaseCiclo ultimaFase;


    public HomeViewModel(@NonNull Application application){
        super(application);
        repo = new CicloRepository(application);
        repoReg = new RegistrazioneRepository(application);

        // Inizializziamo i LiveData di base
        this.cicloActual = repo.getCicloActual();

        // Trasformazione dei giorni di ciclo per confrontarli facilmente nell'Adapter
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

        LiveData<List<CicloConRegistrazioni>> cicliSource = repo.getCicliConRegistrazioni();
        LiveData<List<RegistrazioneConElementi>> registrazioniSource = repoReg.getAll();

        statistiche.addSource(cicliSource, cicli -> {
            ultimiCicli = cicli;
            ricalcolaStatisticheFase();
        });

        statistiche.addSource(registrazioniSource, registrazioni -> {
            ultimeRegistrazioniConElementi = registrazioni;
            ricalcolaStatisticheFase();
        });

        statistiche.addSource(faseSeleccionada, fase -> {
            ultimaFase = fase;
            ricalcolaStatisticheFase();
        });

    }

    private void ricalcolaStatisticheFase() {
        if (ultimaFase == null || ultimiCicli == null || ultimeRegistrazioniConElementi == null) {
            statistiche.setValue(null);
            return;
        }

        StatisticheCalculator.StatisticheResult result = StatisticheCalculator.calcola(ultimiCicli, ultimeRegistrazioniConElementi);
        StatisticheCalculator.StatisticheFase statsFase = result.getStatsFase(ultimaFase);

        statistiche.setValue(statsFase);
    }

    public LiveData<StatisticheCalculator.StatisticheFase> getStatisticheFaseSelezionata() {
        return statistiche;
    }

    public void updateSelectedDate(Date data) {
        if (data == null) return;

        Date dataNormalizzata = CicloRepository.truncarFecha(data);
        dataSelezionata.setValue(dataNormalizzata);

        repo.getExecutor().execute(() -> {
            Ciclo ciclo = repo.getCicloPerData(data);
            FaseCiclo fase = getFaseCiclo(data, ciclo);

            if (fase == null) return;
            faseSeleccionada.postValue(fase);
        });
    }

    public LiveData<Date> getDataSelezionata(){
        return dataSelezionata;
    }

    public LiveData<FaseCiclo> getFaseSeleccionada() {
        return faseSeleccionada;
    }

    public FaseCiclo getFaseCiclo(Date data, Ciclo ciclo){
        if (data == null || ciclo == null) {
            return null;
        }

        Date fechaTruncada = CicloRepository.truncarFecha(data);
        Date inicioTruncado = CicloRepository.truncarFecha(ciclo.getDataInizio());

        int durataTotale = ciclo.getDurataTotale();
        if (durataTotale <= 0) durataTotale = 28;

        int durataPeriodo = ciclo.getDurataPeriodo();
        if (durataPeriodo <= 0) durataPeriodo = 5;

        int giornoCiclo = CicloRepository.difDays(inicioTruncado, fechaTruncada);

        return FaseCicloUtils.calcoloFase(giornoCiclo, durataPeriodo, durataTotale);
    }

    public void calcoloPredict(Date inizioCiclo){
        repo.calcoloGiorniProbabile(inizioCiclo, datas -> {
            List<String> giorniP = new ArrayList<>();
            for (Date d : datas) {
                giorniP.add(dateFormat.format(d));
            }
            diasProbables.postValue(giorniP);
        });
    }

    public void savePassiOggi(int passi){
        repoReg.savePassiOggi(new Date(), passi);
    }

    private void generateFechas() {
        List<Date> fechas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, -30);
        for (int i = 0; i < 60; i++) {
            fechas.add(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        listaFechas.setValue(fechas);
    }

    public LiveData<CicloConRegistrazioni> getCicloActual() { return cicloActual; }
    public LiveData<List<String>> getDiasDeRegla() { return diasDeRegla; }
    public LiveData<List<String>> getDiasProbables() { return diasProbables; }
    public LiveData<List<Date>> getListaFechas() { return listaFechas; }

    public String formatDate(Date date) { return dateFormat.format(date); }

    public void addOrDeletePeriodDay(Date date){
        repo.addOrDeletePeriodDay(date);
    }
}
