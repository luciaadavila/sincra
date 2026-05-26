package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.CicloAdapter;
import com.example.sincra.model.Ciclo;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {

    private RecyclerView cicloRecycler;

    public HistorialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        cicloRecycler = view.findViewById(R.id.historialRecycler);

        List<Ciclo> lista = new ArrayList<>();

        cicloRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        CicloAdapter adapter = new CicloAdapter(lista);
        cicloRecycler.setAdapter(adapter);

        return view;
    }
}