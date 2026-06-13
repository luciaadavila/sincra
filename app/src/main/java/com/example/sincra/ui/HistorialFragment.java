package com.example.sincra.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.CicloAdapter;
import com.example.sincra.model.Ciclo;
import com.example.sincra.viewModel.HistorialViewModel;

import java.util.ArrayList;

public class HistorialFragment extends Fragment {

    private CicloAdapter adapter;

    public HistorialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. inicializar vistas
        RecyclerView cicloRecycler = view.findViewById(R.id.historialRecycler);
        cicloRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. inicializamos el adapter
        adapter = new CicloAdapter(new ArrayList<>(), item -> {
            Ciclo seleccionado = (Ciclo) item;
            StatisticheCicloFragment statistiche = new StatisticheCicloFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("cicloId", seleccionado.getCicloId());
            statistiche.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, statistiche)
                    .addToBackStack(null)
                    .commit();
        });

        cicloRecycler.setAdapter(adapter);

        // 3. inicializamos el viewModel correctamente
        HistorialViewModel viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);

        // 4. Observamos el liveData de forma activa
        viewModel.getHistorialCicli().observe(getViewLifecycleOwner(), cicli -> {
            if (cicli != null) {
                adapter.setList(cicli);
            }
        });
    }
}