package com.example.sincra.database.dao;

import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

public interface RegistrazioneDAO {

    @Insert
    long insert(Registrazione registro);

    @Insert
    void insertRel(RegistroCatalogoRel rel);

    @Update
    void update(Registrazione registro);

    @Query("SELECT * FROM registrazione WHERE cicloId = :cicloId")
    List<RegistrazioneConElementi> getByCicloId(int cicloId);

    @Query("SELECT * FROM registrazione WHERE data = :data LIMIT 1")
    RegistrazioneConElementi getByDate(String data);

    @Transaction
    @Query("SELECT * FROM registrazione")
    List<RegistrazioneConElementi> getRegistrazioniConElementi();

    @Transaction
    @Query("SELECT * FROM registrazione ORDER BY data DESC")
    List<RegistrazioneConElementi> getAll();
}
