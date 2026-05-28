package com.example.sincra.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.PredictionAdapter;
import com.example.sincra.model.PredictSettimana;
import com.example.sincra.viewModel.PredictViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredictFragment extends Fragment {

    private PredictViewModel viewModel;
    private PredictionAdapter adapter;
    private RecyclerView recycler;

    public PredictFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_predict, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // 1. recycler
        recycler = view.findViewById(R.id.predictionRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. adapter
        adapter = new PredictionAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        // 3. view model
        viewModel = new ViewModelProvider(this).get(PredictViewModel.class);
        viewModel.getProxCicli().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                adapter.updateList(data);
            }
        });

        // petición para el cálculo asíncrono
        viewModel.loadPredictions();
    }
}