package com.example.sincra.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "registro_catalogo",
        primaryKeys = {"registroId", "elementoId"},
        foreignKeys = {
                @ForeignKey(
                        entity = Registrazione.class,
                        parentColumns = "registroId",
                        childColumns = "registroId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ElementoCatalogo.class,
                        parentColumns = "elementoId",
                        childColumns = "elementoId",
                        onDelete = ForeignKey.CASCADE
                )
        },

        indices = {
                @Index(value = "registroId"),
                @Index(value = "elementoId")
        }
)

public class RegistroCatalogoRel {
    private int registroId;
    private int elementoId;

    public RegistroCatalogoRel(int registroId, int elementoId) {
        this.registroId = registroId;
        this.elementoId = elementoId;
    }

    public int getRegistroId() {
        return registroId;
    }

    public void setRegistroId(int registroId) {
        this.registroId = registroId;
    }

    public int getElementoId() {
        return elementoId;
    }

    public void setElementoId(int elementoId) {
        this.elementoId = elementoId;
    }
}
