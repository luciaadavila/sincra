package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.PredictionAdapter;
import com.example.sincra.model.PredictSettimana;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredictFragment extends Fragment {


    public PredictFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_predict, container, false);
        RecyclerView recycler = view.findViewById(R.id.predictionRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        List<PredictSettimana> lista = new ArrayList<>();

        lista.add(new PredictSettimana(
                "12 - 18 Junio",
                Arrays.asList(false, false, true, true, true, false, false),
                Arrays.asList(12,13,14,15,16,17,18)
        ));

        lista.add(new PredictSettimana(
                "10 - 16 Julio",
                Arrays.asList(false, true, true, true, false, false, false),
                Arrays.asList(10,11,12,13,14,15,16)
        ));

        lista.add(new PredictSettimana(
                "8 - 14 Agosto",
                Arrays.asList(false, false, true, true, true, true, false),
                Arrays.asList(8,9,10,11,12,13,14)
        ));

        PredictionAdapter adapter = new PredictionAdapter(lista);
        recycler.setAdapter(adapter);
        return view;
    }
}