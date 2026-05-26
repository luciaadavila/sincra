package com.example.sincra.model;

import java.util.ArrayList;
import java.util.List;

public class RegistroDiario {

    private String date;
    private boolean isPeriodo;
    private boolean isGiornoProbabile;
    private int giornoCiclo;

    private List<String> idStatoAnimo;
    private List<String> idSintomi;

    private String notas;

    public RegistroDiario() {
        this.idStatoAnimo = new ArrayList<>();
        this.idSintomi = new ArrayList<>();
    }

    public RegistroDiario(String date,
                          boolean isPeriodo,
                          boolean isGiornoProbabile,
                          int giornoCiclo,
                          List<String> idStatoAnimo,
                          List<String> idSintomi,
                          String notas) {

        this.date = date;
        this.isPeriodo = isPeriodo;
        this.isGiornoProbabile = isGiornoProbabile;
        this.giornoCiclo = giornoCiclo;

        this.idStatoAnimo = (idStatoAnimo != null) ? idStatoAnimo : new ArrayList<>();
        this.idSintomi = (idSintomi != null) ? idSintomi : new ArrayList<>();

        this.notas = notas;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getGiornoCiclo() {
        return giornoCiclo;
    }

    public void setGiornoCiclo(int giornoCiclo) {
        this.giornoCiclo = giornoCiclo;
    }

    public List<String> getIdStatoAnimo() {
        return idStatoAnimo;
    }

    public void setIdStatoAnimo(List<String> idStatoAnimo) {
        this.idStatoAnimo = idStatoAnimo;
    }

    public List<String> getIdSintomi() {
        return idSintomi;
    }

    public void setIdSintomi(List<String> idSintomi) {
        this.idSintomi = idSintomi;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public boolean isPeriodo() {
        return isPeriodo;
    }

    public void setPeriodo(boolean periodo) {
        isPeriodo = periodo;
    }

    public boolean isGiornoProbabile() {
        return isGiornoProbabile;
    }

    public void setGiornoProbabile(boolean giornoProbabile) {
        isGiornoProbabile = giornoProbabile;
    }
}