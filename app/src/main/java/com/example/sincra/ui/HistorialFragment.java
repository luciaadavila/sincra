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
import android.widget.TextView;

import com.example.sincra.R;
import com.example.sincra.adapter.CicloAdapter;
import com.example.sincra.viewModel.HistorialViewModel;

import java.util.ArrayList;

public class HistorialFragment extends Fragment {

    private CicloAdapter adapter;
    private TextView emptyCicloText;

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

        emptyCicloText = view.findViewById(R.id.emptyCicloText);


        RecyclerView cicloRecycler = view.findViewById(R.id.historialRecycler);
        cicloRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CicloAdapter(new ArrayList<>(), ciclo -> {
            if (ciclo == null) return;

            StatisticheCicloFragment statistiche = new StatisticheCicloFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("cicloId", ciclo.getCicloId());
            statistiche.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, statistiche)
                    .addToBackStack(null)
                    .commit();
        });

        cicloRecycler.setAdapter(adapter);

        HistorialViewModel viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);

        viewModel.getHistorialCicli().observe(getViewLifecycleOwner(), cicli -> {
            if (cicli != null) {
                adapter.setList(cicli);

                boolean listaVuota = cicli.isEmpty();
                emptyCicloText.setVisibility(listaVuota ? View.VISIBLE : View.GONE);
                cicloRecycler.setVisibility(listaVuota ? View.GONE : View.VISIBLE);
            }

        });

    }
}