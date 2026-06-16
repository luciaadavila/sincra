package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "elemento_catalogo",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "userId")
        )
public class ElementoCatalogo {
    @PrimaryKey(autoGenerate = true)
    private int elementoId;
    @NonNull
    private String tipo="";
    @NonNull
    private String nome="";
    private long userId;

    @Ignore
    private boolean isSelected;

    public ElementoCatalogo(){}

    public ElementoCatalogo(@NonNull String tipo,@NonNull String nome, long userId){
        this.tipo = tipo;
        this.nome = nome;
        this.userId = userId;
    }


    public int getElementoId() {
        return elementoId;
    }


    public void setElementoId(int elementoId) {
        this.elementoId = elementoId;
    }


    @NonNull
    public String getTipo() {
        return tipo;
    }


    public void setTipo(@NonNull String tipo) {
        this.tipo = tipo;
    }

    @NonNull
    public String getNome() {
        return nome;
    }


    public void setNome(@NonNull String nome) {
        this.nome = nome;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
