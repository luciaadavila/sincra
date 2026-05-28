package com.example.sincra.model.relazioni;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.sincra.model.Ciclo;
import com.example.sincra.model.Registrazione;

import java.util.List;

public class CicloConRegistrazioni {

    @Embedded
    private Ciclo ciclo;

    @Relation(parentColumn = "cicloId", entityColumn = "cicloId")
    private List<Registrazione> registrazioni;

    public Ciclo getCiclo() {
        return ciclo;
    }

    public void setCiclo(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    public List<Registrazione> getRegistrazioni() {
        return registrazioni;
    }

    public void setRegistrazioni(List<Registrazione> registrazioni) {
        this.registrazioni = registrazioni;
    }
}
