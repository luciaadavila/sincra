package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.RegistroAdapter;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.viewModel.RegistroViewModel;

import java.util.ArrayList;
import java.util.List;

public class RegistroFragment extends Fragment {

    private RegistroAdapter adapter;
    private RegistroViewModel viewModel;
    private RecyclerView registroRecycler;

    public RegistroFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registro, container, false);
        registroRecycler = view.findViewById(R.id.registroRecycler);
        registroRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        RegistroAdapter adapter = new RegistroAdapter(new ArrayList<>());
        registroRecycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        viewModel.getRegistro().observe(getViewLifecycleOwner(), data -> {
            adapter.updateList(data);
        });
        return view;
    }
}