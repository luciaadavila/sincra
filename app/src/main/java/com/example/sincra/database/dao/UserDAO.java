package com.example.sincra.database.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sincra.model.User;

import java.util.List;

public interface UserDAO {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM user WHERE userId = :userId")
    User getById(int userId);
}
