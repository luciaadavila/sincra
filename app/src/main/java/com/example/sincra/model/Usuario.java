package com.example.sincra.model;

import java.util.Date;

public class Usuario {
    private String id;
    private String nombre;
    private Date dataRegistro;
    private int durataMediaCiclo;
    private int durataMediaPeriodo;

    public Usuario(){}

    public Usuario(String id, String nombre, Date dataRegistro, int durataMediaCiclo, int durataMediaPeriodo){
        this.id = id;
        this.nombre = nombre;
        this.dataRegistro = dataRegistro;
        this.durataMediaCiclo = durataMediaCiclo;
        this.durataMediaPeriodo = durataMediaPeriodo;
    }


    // getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(Date dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public int getDurataMediaCiclo() {
        return durataMediaCiclo;
    }

    public void setDurataMediaCiclo(int durataMediaCiclo) {
        this.durataMediaCiclo = durataMediaCiclo;
    }

    public int getDurataMediaPeriodo() {
        return durataMediaPeriodo;
    }

    public void setDurataMediaPeriodo(int durataMediaPeriodo) {
        this.durataMediaPeriodo = durataMediaPeriodo;
    }


}
