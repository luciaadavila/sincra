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
}
