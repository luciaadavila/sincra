package com.example.sincra.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.adapter.StatisticheFaseAdapter;
import com.example.sincra.utils.FaseCiclo;
import com.example.sincra.utils.StatisticheCalculator;
import com.example.sincra.viewModel.StatisticheViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticheFragment extends Fragment {

    private StatisticheViewModel viewModel;
    private StatisticheFaseAdapter adapter;

    private LinearLayout summaryContainer;
    private RecyclerView fasiRecycler;

    public StatisticheFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_statistiche, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        summaryContainer = view.findViewById(R.id.summaryContainer);
        fasiRecycler = view.findViewById(R.id.fasiRecycler);

        adapter = new StatisticheFaseAdapter();

        fasiRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        fasiRecycler.setAdapter(adapter);
        fasiRecycler.setNestedScrollingEnabled(false); // Refuerzo por código
        fasiRecycler.setHasFixedSize(false);

        viewModel = new ViewModelProvider(this).get(StatisticheViewModel.class);

        viewModel.getStatistiche().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            summaryContainer.removeAllViews();
            renderSummary(result);

            adapter.setItems(getFasiOrdinate(result));
        });
    }

    private void renderSummary(StatisticheCalculator.StatisticheResult result) {
        LinearLayout row1 = createHorizontalRow();

        row1.addView(createSummaryCard(
                "Cicli",
                String.valueOf(result.getNumeroCicli())
        ));

        row1.addView(createSummaryCard(
                "Durata media",
                result.getDurataMediaCiclo() + " giorni"
        ));

        summaryContainer.addView(row1);

        LinearLayout row2 = createHorizontalRow();

        row2.addView(createSummaryCard(
                "Periodo medio",
                result.getDurataMediaPeriodo() + " giorni"
        ));

        row2.addView(createSummaryCard(
                "Variazione",
                result.getVariazioneMediaCiclo() + " giorni"
        ));

        summaryContainer.addView(row2);

        LinearLayout row3 = createHorizontalRow();

        row3.addView(createSummaryCard(
                "Ciclo più breve",
                result.getCicloMin() + " giorni"
        ));

        row3.addView(createSummaryCard(
                "Ciclo più lungo",
                result.getCicloMax() + " giorni"
        ));

        summaryContainer.addView(row3);
    }

    private List<StatisticheCalculator.StatisticheFase> getFasiOrdinate(
            StatisticheCalculator.StatisticheResult result
    ) {
        List<StatisticheCalculator.StatisticheFase> list = new ArrayList<>();

        list.add(result.getStatsFase(FaseCiclo.MESTRUALE));
        list.add(result.getStatsFase(FaseCiclo.FOLLICOLARE));
        list.add(result.getStatsFase(FaseCiclo.OVULATORIA));
        list.add(result.getStatsFase(FaseCiclo.LUTEALE));

        return list;
    }

    private LinearLayout createHorizontalRow() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rowParams);

        return row;
    }

    private View createSummaryCard(String label, String value) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(12), dp(14), dp(12), dp(14));
        card.setBackgroundResource(R.drawable.bg_stats_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        card.setLayoutParams(params);

        TextView valueText = new TextView(requireContext());
        valueText.setText(value);
        valueText.setTextSize(18);
        valueText.setTypeface(Typeface.DEFAULT_BOLD);
        valueText.setTextColor(0xFF333333);
        valueText.setGravity(Gravity.CENTER);

        TextView labelText = new TextView(requireContext());
        labelText.setText(label);
        labelText.setTextSize(12);
        labelText.setTextColor(0xFF777777);
        labelText.setGravity(Gravity.CENTER);

        card.addView(valueText);
        card.addView(labelText);

        return card;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}