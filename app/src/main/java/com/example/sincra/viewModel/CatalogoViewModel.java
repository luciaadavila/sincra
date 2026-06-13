package com.example.sincra.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.sincra.database.repositorio.ElementoCatalogoRepository;
import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

public class CatalogoViewModel extends AndroidViewModel {

    private final ElementoCatalogoRepository repo;
    private final MutableLiveData<String> tipoInput = new MutableLiveData<>();
    private final LiveData<List<ElementoCatalogo>> items;

    public CatalogoViewModel(@NonNull Application application) {
        super(application);
        repo = new ElementoCatalogoRepository(application);
        items = Transformations.switchMap(tipoInput, repo::getByType);
    }


    public LiveData<List<ElementoCatalogo>> getItems() {
        return items;
    }

    public void loadByType(String tipo) {
        tipoInput.setValue(tipo);
    }

    public void addItem(String nome, String tipo) {
        long localId = getApplication().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                .getLong("local_user_id", -1L);
        if (localId != -1) {
            ElementoCatalogo elemento = new ElementoCatalogo(tipo, nome, localId);
            repo.insert(elemento);
        }
    }

    public void deleteItem(ElementoCatalogo e){
        repo.deleteItem(e);
    }

    public void updateItem(ElementoCatalogo e, String nuovoNome){
        repo.updateItem(e, nuovoNome);
    }
}



