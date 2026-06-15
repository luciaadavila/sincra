package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.util.Date;
import java.util.List;

@Dao
public interface CicloDAO {
    @Insert
    long insert(Ciclo ciclo);

    @Update
    void update(Ciclo ciclo);

    @Delete
    void delete(Ciclo ciclo);

    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId LIMIT 1")
    LiveData<Ciclo> getById(Integer cicloId);

    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId LIMIT 1")
    Ciclo getByIdSync(Integer cicloId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE cicloId = :cicloId LIMIT 1")
    LiveData<CicloConRegistrazioni> getCicloByIdConRegistrazioni(Integer cicloId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE userId = :userId")
    LiveData<List<CicloConRegistrazioni>> getCicliConRegistrazioni(long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    LiveData<List<Ciclo>> getHistorialCicli(long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC")
    List<Ciclo> getHistorialCicliSync(long userId);

    @Transaction
    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC LIMIT 1")
    LiveData<CicloConRegistrazioni> getCicloActualConRegistrazioni(long userId);

    // ciclo actual
    @Query("SELECT * FROM ciclo WHERE userId = :userId ORDER BY dataInizio DESC LIMIT 1")
    Ciclo getCurrentCiclo(long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId AND dataInizio < :fechaInicio ORDER BY dataInizio DESC LIMIT 1")
    Ciclo getCicloAnteriorSync(Date fechaInicio, long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId AND dataInizio <= :fecha ORDER BY dataInizio DESC LIMIT 1")
    Ciclo getCicloPerDataSync(Date fecha, long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId AND dataInizio > :fecha ORDER BY dataInizio ASC LIMIT 1")
    Ciclo getCicloPosteriorSync(Date fecha, long userId);

    @Query("SELECT * FROM ciclo WHERE userId = :userId AND dataFine IS NOT NULL ORDER BY dataInizio DESC LIMIT 4")
    List<Ciclo> getLastFourCicliSync(long userId);
}