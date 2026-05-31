package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.database.repositorio.UserRepository;
import com.example.sincra.model.User;

public class ProfiloViewModel extends AndroidViewModel {
    private final LiveData<User> user;
    private final UserRepository repo;
    private final CicloRepository repoCiclo;

    public ProfiloViewModel(@NonNull Application application){
        super(application);
        repo = new UserRepository(application);
        repoCiclo = new CicloRepository(application);
        this.user = repo.getUserProfilo();
    }

    public LiveData<User> getUserProfilo(){
        return user;
    }

    public void updateUserProfilo(User user){
        repo.updateUserProfilo(user);
    }

    public void updateDurataMedia(){
        repoCiclo.updateDurataMedia();
    }
}
