package com.example.sincra.database.dao;

import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sincra.model.Registrazione;

import java.util.List;

public interface RegistrazioneDAO {

    @Insert
    void insert(Registrazione registro);

    @Update
    void update(Registrazione registro);

    @Query("SELECT * FROM registrazione WHERE cicloId = :cicloId")
    List<Registrazione> getByCicloId(int cicloId);

    @Query("SELECT * FROM registrazione WHERE date = :date LIMIT 1")
    Registrazione getByDate(String date);


}
