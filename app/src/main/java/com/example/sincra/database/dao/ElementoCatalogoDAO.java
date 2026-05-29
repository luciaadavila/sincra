package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

@Dao
public interface ElementoCatalogoDAO {
    @Insert
    void insert(ElementoCatalogo elementoCatalogo);

    @Update
    void update(ElementoCatalogo elementoCatalogo);

    @Delete
    void delete(ElementoCatalogo elementoCatalogo);

    @Query("SELECT * FROM elemento_catalogo WHERE userId = :userId AND tipo = :tipo ORDER BY nome ASC")
    LiveData<List<ElementoCatalogo>> getElementosByUsuarioAndTipo(long userId, String tipo);
}
