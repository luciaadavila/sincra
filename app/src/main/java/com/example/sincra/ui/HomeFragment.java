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
import android.widget.Button;

import com.example.sincra.R;
import com.example.sincra.adapter.CalendarioHorizontalAdapter;
import com.example.sincra.viewModel.HomeViewModel;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {

    private CalendarioHorizontalAdapter adapter;
    private Button dayButton;
    private String fechaSeleccionadaFormateada;
    private HomeViewModel viewModel;
    private RecyclerView dayRecycler;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        dayButton = view.findViewById(R.id.dayButton);
        dayRecycler = view.findViewById(R.id.calendarRecyclerView);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        fechaSeleccionadaFormateada = viewModel.formatDate(new Date());
        dayButton.setText(fechaSeleccionadaFormateada);

        adapter = new CalendarioHorizontalAdapter(new ArrayList<>(), new CalendarioHorizontalAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(Date fechaSeleccionada) {
                fechaSeleccionadaFormateada = viewModel.formatDate(fechaSeleccionada);
                dayButton.setText(fechaSeleccionadaFormateada);
            }

            @Override
            public void onDateDoubleClick(Date fechaSeleccionada) {
                viewModel.addOrDeletePeriodDay(fechaSeleccionada);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        dayRecycler.setLayoutManager(layoutManager);
        dayRecycler.setAdapter(adapter);

        viewModel.getCicloActual().observe(getViewLifecycleOwner(), cicloConReg -> {
            if (cicloConReg != null && cicloConReg.getCiclo().getDataInizio() != null) {
                viewModel.calcoloPredict(cicloConReg.getCiclo().getDataInizio());
            }
        });

        viewModel.getListaFechas().observe(getViewLifecycleOwner(), fechas -> {
            if (fechas != null) {
                adapter.setListaFechas(fechas);
                dayRecycler.scrollToPosition(15);
            }
        });

        viewModel.getDiasDeRegla().observe(getViewLifecycleOwner(), diasDeRegla -> {
            if (diasDeRegla != null) {
                adapter.setFechasConPeriodo(diasDeRegla);
            }
        });

        viewModel.getDiasProbables().observe(getViewLifecycleOwner(), diasProbables -> {
            if (diasProbables != null) {
                adapter.setDiasProbables(diasProbables);
            }
        });

        dayButton.setOnClickListener(v -> {
            DetailDayFragment detailFragment = new DetailDayFragment();
            Bundle args = new Bundle();
            args.putString("date", fechaSeleccionadaFormateada);
            detailFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
