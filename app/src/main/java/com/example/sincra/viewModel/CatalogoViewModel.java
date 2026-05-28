package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.sincra.database.repositorio.ElementoCatalogoRepository;
import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

public class CatalogoViewModel extends AndroidViewModel {

    private final ElementoCatalogoRepository repo;

    private final MutableLiveData<List<ElementoCatalogo>> items =
            new MutableLiveData<>();

    public CatalogoViewModel(@NonNull Application application) {
        super(application);
        repo = new ElementoCatalogoRepository(application);
    }

    public MutableLiveData<List<ElementoCatalogo>> getItems() {
        return items;
    }

    public void loadByType(String tipo) {
        repo.getByType(tipo, data -> {
            items.postValue(data);
        });
    }

    public void addItem(String nome, String tipo) {
        ElementoCatalogo elemento =
                new ElementoCatalogo(tipo, nome, 1);

        repo.insert(elemento);
        loadByType(tipo);
    }

    public void deleteItem(ElementoCatalogo elemento) {

        repo.delete(elemento);

        loadByType(elemento.getTipo());
    }
}