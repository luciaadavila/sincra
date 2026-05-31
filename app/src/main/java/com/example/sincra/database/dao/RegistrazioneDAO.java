package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.Date;
import java.util.List;

@Dao
public interface RegistrazioneDAO {

    @Insert
    long insert(Registrazione registro);

    @Insert
    long insertRel(RegistroCatalogoRel rel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(Registrazione registrazione);

    @Query("DELETE FROM registro_catalogo WHERE registroId = :registroId")
    void deleteRelByRegistroId(int registroId);

    @Update
    void update(Registrazione registro);

    @Transaction
    @Query("SELECT * FROM registrazione WHERE cicloId = :cicloId")
    LiveData<List<RegistrazioneConElementi>> getByCicloId(int cicloId);

    @Transaction
    @Query("SELECT r.* FROM registrazione r " +
            "INNER JOIN ciclo c ON r.cicloId = c.cicloId " +
            "WHERE r.data = :data AND c.userId = :userId LIMIT 1")
    LiveData<RegistrazioneConElementi> getByDateAndUser(Date data, long userId);

    @Transaction
    @Query("SELECT r.* FROM registrazione r " +
            "INNER JOIN ciclo c ON r.cicloId = c.cicloId " +
            "WHERE r.data = :date AND c.userId = :userId LIMIT 1")
    Registrazione getRegistroByDate(Date date, long userId);

    @Transaction
    @Query("SELECT r.* FROM registrazione r " +
            "INNER JOIN ciclo c ON r.cicloId = c.cicloId " +
            "WHERE c.userId = :userId " +
            "ORDER BY r.data DESC")
    LiveData<List<RegistrazioneConElementi>> getAllByUserId(long userId);

    @Transaction
    default void insertRegistroCompleto(Registrazione registro, List<Integer> elementoIds) {
        long registroId;
        if (registro.getRegistroId() != 0) {
            update(registro);
            registroId = registro.getRegistroId();
            deleteRelByRegistroId((int) registroId);
        } else {
            registroId = insert(registro);
        }

        for (Integer elementoId : elementoIds) {
            RegistroCatalogoRel rel = new RegistroCatalogoRel((int) registroId, elementoId);
            insertRel(rel);
        }
    }

    // Adaptado con r.data para mantener coherencia con tus otras queries de Room
    @Transaction
    @Query("SELECT r.* FROM registrazione r " +
            "INNER JOIN ciclo c ON r.cicloId = c.cicloId " +
            "WHERE r.cicloId = :cicloId AND r.data >= :desdeFecha AND c.userId = :userId " +
            "ORDER BY r.data ASC")
    List<Registrazione> getRegistrosPosterioresSync(int cicloId, Date desdeFecha, long userId);

    @Query("SELECT r.data FROM registrazione r " +
            "INNER JOIN ciclo c ON r.cicloId = c.cicloId " +
            "WHERE c.userId = :userId AND r.isPeriodo = 1")
    LiveData<List<Date>> getFechasConPeriodoByUserId(long userId);
}
