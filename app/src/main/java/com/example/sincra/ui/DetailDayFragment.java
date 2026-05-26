package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoAdapter;
import com.example.sincra.model.ElementoCatalogo;

import java.util.ArrayList;
import java.util.List;

public class DetailDayFragment extends Fragment {


    public DetailDayFragment() {
        // Required empty public constructor
    }

    // se crea la UI del fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // se convierte el xml en una pantalla real
        View view = inflater.inflate(R.layout.fragment_detail_day, container, false);

        RecyclerView moodRecycler = view.findViewById(R.id.moodRecycler);
        RecyclerView symptomRecycler = view.findViewById(R.id.symptomRecycler);

        moodRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        symptomRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ElementoCatalogo> moods = new ArrayList<>();
        moods.add(new ElementoCatalogo("Feliz", "mood"));
        moods.add(new ElementoCatalogo("Estranna", "mood"));

        List<ElementoCatalogo> symptoms = new ArrayList<>();
        symptoms.add(new ElementoCatalogo("Dolor abdominal", "sintoma"));
        symptoms.add(new ElementoCatalogo("Dolor cabeza", "sintoma"));

        CatalogoAdapter moodAdapter = new CatalogoAdapter(moods);
        CatalogoAdapter symptomAdapter = new CatalogoAdapter(symptoms);

        moodRecycler.setAdapter(moodAdapter);
        symptomRecycler.setAdapter(symptomAdapter);

        return view;
    }
}