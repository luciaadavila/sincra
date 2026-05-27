package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "elemento_catalogo")
public class ElementoCatalogo {
    @PrimaryKey(autoGenerate = true)
    private String id;
    @NonNull
    private String tipo;
    @NonNull
    private String nome;
    private String icona;
    private boolean isPersonalizzato;
    private boolean selected;

    public ElementoCatalogo(){}

    public ElementoCatalogo(String id, @NonNull String tipo,@NonNull String nome, String icona, boolean ePersonalizzato, boolean selected){
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.icona = icona;
        this.isPersonalizzato = ePersonalizzato;
        this.selected= selected;
    }

    public ElementoCatalogo(@NonNull String nome, @NonNull String tipo){
        this.nome = nome;
        this.tipo = tipo;
        this.selected = false;
    }

    // getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getIcona() {
        return icona;
    }

    public void setIcona(String icona) {
        this.icona = icona;
    }

    public boolean isPersonalizzato() {
        return isPersonalizzato;
    }

    public void setPersonalizzato(boolean isPersonalizzato) {
        this.isPersonalizzato = isPersonalizzato;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
