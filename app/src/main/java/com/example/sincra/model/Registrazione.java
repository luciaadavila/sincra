package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "registrazione",
        foreignKeys = @ForeignKey(
                entity = Ciclo.class,
                parentColumns = "cicloId",
                childColumns = "cicloId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "cicloId")
)


public class Registrazione {

    @PrimaryKey(autoGenerate = true)
    private int registroId;

    private int cicloId;
    @NonNull
    private Date data;

    private int pasos;

    private boolean isGiornoProbabile;
    private boolean isPeriodo;
    private int giornoCiclo;

    //private List<ElementoCatalogo> statiAnimo;
    //private List<ElementoCatalogo> sintomi;

    private String notas;

    @Ignore
    public Registrazione(@NonNull Date data) {
        this.data = data;
    }

    public Registrazione(@NonNull Date data,
                         boolean isPeriodo,
                         boolean isGiornoProbabile,
                         int giornoCiclo,
                         String notas,
                         int cicloId, int pasos) {

        this.data = data;
        this.isPeriodo = isPeriodo;
        this.isGiornoProbabile = isGiornoProbabile;
        this.giornoCiclo = giornoCiclo;
        this.cicloId = cicloId;
        this.pasos = pasos;

        //this.statiAnimo = statiAnimo != null ? statiAnimo : new ArrayList<>();
        //this.sintomi = sintomi != null ? sintomi : new ArrayList<>();

        this.notas = notas;
    }

    @NonNull
    public Date getData() {
        return data;
    }

    public Date getDate() {
        return data;
    }

    public void setData(@NonNull Date data) {
        this.data = data;
    }

    public void setDate(@NonNull Date data) {
        this.data = data;
    }

    public int getGiornoCiclo() {
        return giornoCiclo;
    }

    public void setGiornoCiclo(int giornoCiclo) {
        this.giornoCiclo = giornoCiclo;
    }


    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

    /*
    public List<ElementoCatalogo> getStatiAnimo() {
        return statiAnimo;
    }

    public void setStatiAnimo(List<ElementoCatalogo> statiAnimo) {
        this.statiAnimo = statiAnimo;
    }*/

    /*public List<ElementoCatalogo> getSintomi(){
        return sintomi;
    }*/

    /*public void setSintomi(List<ElementoCatalogo> sintomo){
        this.sintomi = sintomo;
    }*/

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

    public int getCicloId() {
        return cicloId;
    }

    public void setCicloId(int cicloId) {
        this.cicloId = cicloId;
    }

    public int getRegistroId() {
        return registroId;
    }

    public int getPasos() {
        return pasos;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }


}