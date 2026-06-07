package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

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

    private Integer cicloId;
    @NonNull
    private Date data;

    private int passi;

    private boolean isPeriodo;
    private int giornoCiclo;

    @Ignore
    public Registrazione(@NonNull Date data) {
        this.data = data;
    }

    public Registrazione(@NonNull Date data,
                         boolean isPeriodo,
                         int giornoCiclo,
                         Integer cicloId, 
                         int passi) {

        this.data = data;
        this.isPeriodo = isPeriodo;
        this.giornoCiclo = giornoCiclo;
        this.cicloId = cicloId;
        this.passi = passi;
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

    public boolean isPeriodo() {
        return isPeriodo;
    }

    public void setPeriodo(boolean periodo) {
        isPeriodo = periodo;
    }

    public Integer getCicloId() {
        return cicloId;
    }

    public void setCicloId(Integer cicloId) {
        this.cicloId = cicloId;
    }

    public int getRegistroId() {
        return registroId;
    }

    public int getPassi() {
        return passi;
    }

    public void setPassi(int passi) {
        this.passi = passi;
    }
}
