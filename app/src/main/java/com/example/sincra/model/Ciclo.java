package com.example.sincra.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ciclo {
    private String id;
    private Date dataInizio;
    private Date dataFine;
    private int durataTotale;
    private int durataPeriodo;
    private List<String> sintomiComuni;
    private List<RegistroDiario> registrazioni;

    public Ciclo(){
        this.sintomiComuni = new ArrayList<>();
        this.registrazioni = new ArrayList<>();
    }

    public Ciclo(String id, Date dataInizio, Date dataFine, int durataTotale, int durataPeriodo, List<String> sintomiComuni, List<RegistroDiario> registrazioni){
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.durataTotale = durataTotale;
        this.durataPeriodo = durataPeriodo;
        this.sintomiComuni = sintomiComuni != null ? sintomiComuni : new ArrayList<>();
        this.registrazioni = registrazioni != null ? registrazioni : new ArrayList<>();
    }


    // getters y setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(Date dataInizio) {
        this.dataInizio = dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public void setDataFine(Date dataFine) {
        this.dataFine = dataFine;
    }

    public int getDurataTotale() {
        return durataTotale;
    }

    public void setDurataTotale(int durataTotale) {
        this.durataTotale = durataTotale;
    }

    public int getDurataPeriodo() {
        return durataPeriodo;
    }

    public void setDurataPeriodo(int durataPeriodo) {
        this.durataPeriodo = durataPeriodo;
    }

    public List<String> getSintomiComuni() {
        return sintomiComuni;
    }

    public void setSintomiComuni(List<String> sintomiComuni) {
        this.sintomiComuni = sintomiComuni;
    }

    public List<RegistroDiario> getRegistrazioni() {
        return registrazioni;
    }

    public void setRegistrazioni(List<RegistroDiario> registrazioni) {
        this.registrazioni = registrazioni;
    }
}
