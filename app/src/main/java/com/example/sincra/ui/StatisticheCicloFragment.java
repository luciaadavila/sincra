package com.example.sincra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sincra.R;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.viewModel.StatisticheCicloViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StatisticheCicloFragment extends Fragment {

    private LinearLayout tableContainer;
    private int cicloId;
    private StatisticheCicloViewModel viewModel;

    public StatisticheCicloFragment() {
    }

    public static StatisticheCicloFragment newInstance(int cicloId) {
        StatisticheCicloFragment fragment = new StatisticheCicloFragment();
        Bundle args = new Bundle();
        args.putInt("cicloId", cicloId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistiche_ciclo, container, false);
        tableContainer = view.findViewById(R.id.tableContainer);

        Bundle args = getArguments();

        if (getArguments() != null) {
            cicloId = getArguments().getInt("cicloId");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatisticheCicloViewModel.class);
        viewModel.setCicloId(cicloId);

        viewModel.getCicloConRegistrazioni().observe(getViewLifecycleOwner(), cicloConRegistrazioni -> {
            if (cicloConRegistrazioni != null){
                tableContainer.removeAllViews();
                generateTable(cicloConRegistrazioni);
            }
        });
    }

    private void generateTable(CicloConRegistrazioni datosCiclo) {
        int totalDays = datosCiclo.getCiclo().getDurataTotale();
        List<Registrazione> registrazioni = datosCiclo.getRegistrazioni() != null ?
                datosCiclo.getRegistrazioni() : new ArrayList<>();

        // 1. Fila de Días (Giorno 1, Giorno 2...)
        addRow("Giorno", totalDays, i -> String.valueOf(i + 1));

        // 2. Fila de Periodo (🔴 si ese día de ciclo tiene periodo)
        addRow("Periodo", totalDays, i -> {
            Registrazione reg = buscarRegistroPorDiaCiclo(registrazioni, i + 1);
            return (reg != null && reg.isPeriodo()) ? "🔴" : "";
        });

        // 3. Fila de Notas (O Pasos, ya que Mood y Sintomi requieren otra consulta cruzada)
        addRow("Nota", totalDays, i -> {
            Registrazione reg = buscarRegistroPorDiaCiclo(registrazioni, i + 1);
            return (reg != null && reg.getNotas() != null) ? reg.getNotas() : "";
        });

        // 4. Fila estática de Pasos (Como la tenías de muestra)
        addRow("Passi", totalDays, i -> String.valueOf((i + 1) * 1000));
    }

    private Registrazione buscarRegistroPorDiaCiclo(List<Registrazione> lista, int numeroDiaCiclo) {
        for (Registrazione r : lista) {
            if (r.getGiornoCiclo() == numeroDiaCiclo) {
                return r;
            }
        }
        return null; // Si el usuario no registró ese día, devolvemos null de forma segura sin romper la app
    }

    private void addRow(String label, int totalDays, Function<Integer, String> cellProvider) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Nombre de la fila
        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setPadding(20, 20, 20, 20);
        row.addView(labelView);

        // Celdas dinámicas
        for (int i = 0; i < totalDays; i++) {
            TextView cell = new TextView(getContext());
            cell.setText(cellProvider.apply(i));
            cell.setPadding(30, 20, 30, 20);
            row.addView(cell);
        }

        tableContainer.addView(row);
    }
}