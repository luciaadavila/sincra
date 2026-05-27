package com.example.sincra.database;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;

@Database(entities = {
            ElementoCatalogo.class,
            Registrazione.class},
        version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ElementoCatalogoDAO elementoCatalogoDAO();
    public abstract RegistrazioneDAO registrazioneDAO();
}
