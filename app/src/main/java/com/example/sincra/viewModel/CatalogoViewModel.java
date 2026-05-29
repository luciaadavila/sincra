package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.ElementoCatalogoRepository;
import com.example.sincra.model.ElementoCatalogo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CatalogoViewModel extends AndroidViewModel {

    private final ElementoCatalogoRepository repo;
    private final MutableLiveData<String> tipoInput = new MutableLiveData<>();
    private final LiveData<List<ElementoCatalogo>> items;

    public CatalogoViewModel(@NonNull Application application) {
        super(application);
        repo = new ElementoCatalogoRepository(application);
        items = Transformations.switchMap(tipoInput, tipo -> {
            return repo.getByType(tipo);
        });
    }


    public LiveData<List<ElementoCatalogo>> getItems() {
        return items;
    }

    public void loadByType(String tipo) {
        tipoInput.setValue(tipo);
    }

    public void addItem(String nome, String tipo) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ElementoCatalogo elemento = new ElementoCatalogo(tipo, nome, user.getUid());
            repo.insert(elemento);
        }
    }

    public void deleteItem(ElementoCatalogo elemento) {
        repo.delete(elemento);
    }
}