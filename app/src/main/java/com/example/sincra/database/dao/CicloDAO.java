package com.example.sincra.database.dao;

import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.List;

public interface CicloDAO {
    @Insert
    void insert(Ciclo ciclo);

    @Update
    void update(Ciclo ciclo);

    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId")
    Ciclo getById(int cicloId);

    @Transaction
    @Query("SELECT * FROM ciclo")
    List<CicloConRegistrazioni> getCicliConRegistrazioni();

}
