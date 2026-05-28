package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;
    @NonNull
    private String nombre;
    private Date dataRegistro;
    private int durataMediaCiclo;
    private int durataMediaPeriodo;

    public User(){}

    public User(@NonNull String nombre, Date dataRegistro, int durataMediaCiclo, int durataMediaPeriodo){
        this.nombre = nombre;
        this.dataRegistro = dataRegistro;
        this.durataMediaCiclo = durataMediaCiclo;
        this.durataMediaPeriodo = durataMediaPeriodo;
    }


    // getters y setters
    public int getUserId() {
        return userId;
    }

    @NonNull
    public String getNombre() {
        return nombre;
    }

    public void setNombre(@NonNull String nombre) {
        this.nombre = nombre;
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
