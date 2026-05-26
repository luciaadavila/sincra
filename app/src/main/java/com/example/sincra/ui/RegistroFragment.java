package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.RegistroAdapter;
import com.example.sincra.model.RegistroDiario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistroFragment extends Fragment {

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

        List<RegistroDiario> lista = new ArrayList<>();

        RegistroDiario d1 = new RegistroDiario(
                "2026-05-01",
                true,
                false,
                3,
                Arrays.asList("Feliz", "Energica"),
                Arrays.asList("Ninguno"),
                "Buen día"
        );

        RegistroDiario d2 = new RegistroDiario(
                "2026-05-02",
                false,
                false,
                4,
                Arrays.asList("Cansada"),
                Arrays.asList("Dolor cabeza"),
                ""
        );

        lista.add(d1);
        lista.add(d2);


        RegistroAdapter adapter = new RegistroAdapter(lista);
        registroRecycler.setAdapter(adapter);
        return view;
    }
}