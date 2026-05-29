package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoEditableAdapter;
import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.viewModel.CatalogoViewModel;

import java.util.ArrayList;
import java.util.List;


public class CatalogoEditableFragment extends Fragment {

    private CatalogoEditableAdapter adapter;
    private CatalogoViewModel viewModel;
    private String tipo;

    public CatalogoEditableFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_catalogo_editable, container, false);

        TextView title = view.findViewById(R.id.titleText);
        EditText input = view.findViewById(R.id.inputNew);
        Button addButton = view.findViewById(R.id.addButton);
        RecyclerView recycler = view.findViewById(R.id.catalogRecycler);

        tipo = getArguments() != null ? getArguments().getString("tipo") : "mood";
        title.setText(tipo.equals("mood") ? "Stati d'animo" : "Sintomi");

        viewModel = new ViewModelProvider(this).get(CatalogoViewModel.class);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CatalogoEditableAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), data -> {
            adapter.updateList(data);
        });

        viewModel.loadByType(tipo);


        addButton.setOnClickListener(v -> {
            String inputText = input.getText().toString().trim();

            if (!inputText.isEmpty()){
                viewModel.addItem(inputText, tipo);
                input.setText("");
            }
        });

        return view;
    }
}