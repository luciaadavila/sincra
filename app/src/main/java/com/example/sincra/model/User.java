package com.example.sincra.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long userId;
    @NonNull
    private String firebaseUid;
    @NonNull
    private String nombre;
    private Date dataRegistro;
    private int durataMediaCiclo;
    private int durataMediaPeriodo;

    public User(){}

    public User(@NonNull String firebaseUid, @NonNull String nombre, Date dataRegistro, int durataMediaCiclo, int durataMediaPeriodo){
        this.firebaseUid = firebaseUid;
        this.nombre = nombre;
        this.dataRegistro = dataRegistro;
        this.durataMediaCiclo = durataMediaCiclo;
        this.durataMediaPeriodo = durataMediaPeriodo;
    }

    public User(User other){
        this.userId = other.getUserId();
        this.firebaseUid = other.getFirebaseUid();
        this.nombre = other.getNombre();
        this.dataRegistro = other.getDataRegistro();
        this.durataMediaCiclo = other.getDurataMediaCiclo();
        this.durataMediaPeriodo = other.getDurataMediaPeriodo();
    }


    // getters y setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @NonNull
    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(@NonNull String firebaseUid) {
        this.firebaseUid = firebaseUid;
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
