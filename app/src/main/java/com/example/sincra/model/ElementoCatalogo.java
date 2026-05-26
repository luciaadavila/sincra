package com.example.sincra.model;

public class ElementoCatalogo {
    private String id;
    private String tipo;
    private String nome;
    private String icona;
    private boolean ePersonalizzato;
    private boolean selected;

    public ElementoCatalogo(){}

    public ElementoCatalogo(String id, String tipo, String nome, String icona, boolean ePersonalizzato, boolean selected){
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.icona = icona;
        this.ePersonalizzato = ePersonalizzato;
        this.selected= selected;
    }

    public ElementoCatalogo(String nome, String tipo){
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIcona() {
        return icona;
    }

    public void setIcona(String icona) {
        this.icona = icona;
    }

    public boolean isePersonalizzato() {
        return ePersonalizzato;
    }

    public void setePersonalizzato(boolean ePersonalizzato) {
        this.ePersonalizzato = ePersonalizzato;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
