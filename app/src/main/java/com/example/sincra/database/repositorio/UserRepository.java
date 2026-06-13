package com.example.sincra.database.repositorio;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.UserDAO;
import com.example.sincra.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDAO dao;
    private final Context context;
    private final ExecutorService executor;

    public UserRepository(Context context){
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.userDAO();
        executor = Executors.newSingleThreadExecutor();

    }

    private long getLocalId() {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getLong("local_user_id", -1L);
    }

    public LiveData<User> getUserProfilo(){
        return dao.getById(getLocalId());
    }

    public void updateUserProfilo(User user){
        executor.execute(() -> dao.update(user));
    }
}
