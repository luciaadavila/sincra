package com.example.sincra.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.sincra.database.dao.CicloDAO;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.database.dao.RegistrazioneDAO;
import com.example.sincra.database.dao.UserDAO;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.RegistroCatalogoRel;
import com.example.sincra.model.User;

@Database(entities = {
            ElementoCatalogo.class,
            Registrazione.class,
            Ciclo.class,
            User.class,
            RegistroCatalogoRel.class},
        version = 4)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ElementoCatalogoDAO elementoCatalogoDAO();
    public abstract RegistrazioneDAO registrazioneDAO();
    public abstract CicloDAO cicloDAO();
    public abstract UserDAO userDAO();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sincra_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
