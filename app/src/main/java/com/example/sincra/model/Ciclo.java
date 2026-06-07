package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ciclo",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "userId")
)
public class Ciclo {
    @PrimaryKey(autoGenerate = true)
    private int cicloId;
    @NonNull
    private Date dataInizio;
    private Date dataFine;
    private int durataTotale;
    private int durataPeriodo;
    private long userId;

    public Ciclo(){
    }

    public Ciclo(@NonNull Date dataInizio, Date dataFine, int durataTotale, int durataPeriodo, long userId){
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.durataTotale = durataTotale;
        this.durataPeriodo = durataPeriodo;
        this.userId = userId;
    }

    public int getCicloId() {
        return cicloId;
    }

    public void setCicloId(int cicloId) {
        this.cicloId = cicloId;
    }

    @NonNull
    public Date getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(@NonNull Date dataInizio) {
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
