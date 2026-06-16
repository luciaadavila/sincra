package com.example.sincra.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sincra.model.User;

@Dao
public interface UserDAO {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE userId = :userId")
    LiveData<User> getById(long userId);

    @Query("SELECT * FROM users WHERE userId = :userId")
    User getByIdSync(long userId);

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    User getByFirebaseUidSync(String uid);

    @Query("SELECT userId FROM users WHERE firebaseUid = :uid LIMIT 1")
    long getLocalIdByFirebaseUidSync(String uid);
}
