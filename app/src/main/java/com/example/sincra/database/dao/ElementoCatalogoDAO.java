package com.example.sincra.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

public interface ElementoCatalogoDAO {
    @Insert
    void insert(ElementoCatalogo elementoCatalogo);

    @Update
    void update(ElementoCatalogo elementoCatalogo);

    @Delete
    void delete(ElementoCatalogo elementoCatalogo);

    @Query("SELECT * FROM elemento_catalogo")
    List<ElementoCatalogo> getAll();

    @Query("SELECT * FROM elemento_catalogo WHERE tipo = :tipo")
    List<ElementoCatalogo> getByType(String tipo);

}
