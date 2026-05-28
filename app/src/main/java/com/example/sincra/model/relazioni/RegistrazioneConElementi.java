package com.example.sincra.model.relazioni;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;

import java.util.List;

public class RegistrazioneConElementi {
    @Embedded
    public Registrazione registrazione;

    @Relation(
            parentColumn = "registroId",
            entityColumn = "elementoId",
            associateBy = @Junction(RegistroCatalogoRel.class)
    )
    public List<ElementoCatalogo> elementiCatalogo;
}
