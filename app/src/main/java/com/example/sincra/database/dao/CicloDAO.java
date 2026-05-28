package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.List;

@Dao
public interface CicloDAO {
    @Insert
    void insert(Ciclo ciclo);

    @Update
    void update(Ciclo ciclo);

    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId AND userId = :userId")
    LiveData<Ciclo> getById(int cicloId, int userId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE userId = :userId")
    LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni(int userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    LiveData<List<Ciclo>> getHistorialCicli(int userId);

    // una sin liveData para consultas asincronas
    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    List<Ciclo> getHistorialCicliSync(int userId);
}
