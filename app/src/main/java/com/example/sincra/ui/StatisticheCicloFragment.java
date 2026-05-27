package com.example.sincra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.sincra.R;
import com.example.sincra.model.Ciclo;

import java.io.Serializable;
import java.util.function.Function;

public class StatisticheCicloFragment extends Fragment {

    private LinearLayout tableContainer;

    private Ciclo ciclo;

    public StatisticheCicloFragment() {
    }

    public static StatisticheCicloFragment newInstance(Ciclo ciclo) {

        StatisticheCicloFragment fragment =
                new StatisticheCicloFragment();

        Bundle args = new Bundle();

        args.putSerializable("ciclo", (Serializable) ciclo);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_statistiche_ciclo,
                container,
                false
        );

        tableContainer = view.findViewById(R.id.tableContainer);

        Bundle args = getArguments();

        if (args != null) {
            ciclo = (Ciclo) args.getSerializable("ciclo");
        }

        if (ciclo != null) {
            generateTable(ciclo);
        }

        return view;
    }

    private void generateTable(Ciclo ciclo) {

        int totalDays = ciclo.getDurataTotale();

        addRow("Giorno",
                totalDays,
                i -> String.valueOf(i + 1));

        addRow("Periodo",
                totalDays,
                i -> ciclo.getRegistrazione(i).isPeriodo()
                        ? "🔴"
                        : "");

        addRow("Mood",
                totalDays,
                i -> ciclo.getRegistrazione(i)
                        .getStatiAnimo()
                        .toString());

        addRow("Sintomi",
                totalDays,
                i -> ciclo.getRegistrazione(i)
                        .getSintomi()
                        .toString());

        addRow("Passi",
                totalDays,
                i -> String.valueOf((i + 1) * 1000));
    }

    private void addRow(String label,
                        int totalDays,
                        Function<Integer, String> cellProvider) {

        LinearLayout row = new LinearLayout(getContext());

        row.setOrientation(LinearLayout.HORIZONTAL);

        // nombre fila
        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setPadding(20, 20, 20, 20);
        row.addView(labelView);

        for (int i = 0; i < totalDays; i++) {

            TextView cell = new TextView(getContext());
            cell.setText(cellProvider.apply(i));
            cell.setPadding(30, 20, 30, 20);
            row.addView(cell);
        }

        tableContainer.addView(row);
    }
}