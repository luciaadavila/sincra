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
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;

import java.util.ArrayList;
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

        List<Registrazione> lista = new ArrayList<>();

        List<ElementoCatalogo> mood = new ArrayList<>();

        mood.add(new ElementoCatalogo("Feliz", "mood"));
        mood.add(new ElementoCatalogo("Triste", "mood"));
        List<ElementoCatalogo> sintomi = new ArrayList<>();

        sintomi.add(new ElementoCatalogo("Dolor de cabeza", "sintoma"));
        sintomi.add(new ElementoCatalogo("Dolor de garganta", "sintoma"));

        Registrazione d1 = new Registrazione(
                "2026-05-01",
                true,
                false,
                3,
                sintomi,
                mood,
                "Buen día"
        );

        Registrazione d2 = new Registrazione(
                "2026-05-02",
                false,
                false,
                4,
                mood,
                sintomi,
                ""
        );

        lista.add(d1);
        lista.add(d2);


        RegistroAdapter adapter = new RegistroAdapter(lista);
        registroRecycler.setAdapter(adapter);
        return view;
    }
}