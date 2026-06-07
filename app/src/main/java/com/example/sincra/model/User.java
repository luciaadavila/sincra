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
    private String nome;
    private Date dataRegistrazione;
    private int durataMediaCiclo;
    private int durataMediaPeriodo;

    public User(){}

    public User(@NonNull String firebaseUid, @NonNull String nome, Date dataRegistrazione, int durataMediaCiclo, int durataMediaPeriodo){
        this.firebaseUid = firebaseUid;
        this.nome = nome;
        this.dataRegistrazione = dataRegistrazione;
        this.durataMediaCiclo = durataMediaCiclo;
        this.durataMediaPeriodo = durataMediaPeriodo;
    }

    public User(User other){
        this.userId = other.getUserId();
        this.firebaseUid = other.getFirebaseUid();
        this.nome = other.getNome();
        this.dataRegistrazione = other.getDataRegistrazione();
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
    public String getNome() {
        return nome;
    }

    public void setNome(@NonNull String nome) {
        this.nome = nome;
    }

    public Date getDataRegistrazione() {
        return dataRegistrazione;
    }

    public void setDataRegistrazione(Date dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
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
