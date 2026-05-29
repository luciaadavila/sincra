package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.List;

@Dao
public interface RegistrazioneDAO {

    @Insert
    long insert(Registrazione registro);

    @Insert
    long insertRel(RegistroCatalogoRel rel);

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
                  "WHERE r.data = :dataTimestamp AND c.userId = :userId LIMIT 1")
    LiveData<RegistrazioneConElementi> getByDateAndUser(long dataTimestamp, long userId);
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
}
