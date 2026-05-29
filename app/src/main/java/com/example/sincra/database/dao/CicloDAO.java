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

    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId LIMIT 1")
    LiveData<Ciclo> getById(int cicloId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId LIMIT 1")
    LiveData<CicloConRegistrazioni> getCicloByIdConRegistrazioni(int cicloId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE userId = :userId")
    LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni(String userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    LiveData<List<Ciclo>> getHistorialCicli(String userId);

    // una sin liveData para consultas asincronas
    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    List<Ciclo> getHistorialCicliSync(String userId);

    // conseguimos el ciclo actual
    @Transaction
    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC LIMIT 1")
    LiveData<CicloConRegistrazioni> getCicloActualConRegistrazioni(String userId);
}
