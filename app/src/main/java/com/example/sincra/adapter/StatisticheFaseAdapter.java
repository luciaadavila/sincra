package com.example.sincra.adapter;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.utils.StatisticheCalculator;

import java.util.ArrayList;
import java.util.List;

public class StatisticheFaseAdapter extends RecyclerView.Adapter<StatisticheFaseAdapter.FaseViewHolder> {

    private List<StatisticheCalculator.StatisticheFase> items = new ArrayList<>();

    public void setItems(List<StatisticheCalculator.StatisticheFase> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistiche_fase, parent, false);

        return new FaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaseViewHolder holder, int position) {
        StatisticheCalculator.StatisticheFase faseStats = items.get(position);

        holder.sintomiContainer.removeAllViews();
        holder.moodContainer.removeAllViews();

        holder.itemView.setVisibility(View.VISIBLE);
        
        if (faseStats == null || faseStats.getFase() == null) {
            holder.faseTitle.setText(R.string.dati_non_disponibili);
            holder.faseSubtitle.setText("");
            return;
        }

        holder.faseTitle.setText(faseStats.getFase().getResId());

        holder.faseSubtitle.setText(
                holder.itemView.getContext().getString(
                        R.string.stats_fase_subtitle,
                        faseStats.getNumeroRegistrazioni(),
                        faseStats.getMediaPassi()
                )
        );

        addTopElementos(
                holder.sintomiContainer,
                faseStats.getTopSintomi(3)
        );

        addTopElementos(
                holder.moodContainer,
                faseStats.getTopMood(3)
        );
    }

    private void addTopElementos(
            LinearLayout container,
            List<StatisticheCalculator.ElementoStat> elementos
    ) {
        if (elementos == null || elementos.isEmpty()) {
            TextView empty = new TextView(container.getContext());
            empty.setText(R.string.nessun_dato);
            empty.setTextSize(13);
            empty.setTextColor(0xFF999999);
            container.addView(empty);
            return;
        }

        int max = 0;

        for (StatisticheCalculator.ElementoStat elemento : elementos) {
            if (elemento.getCount() > max) {
                max = elemento.getCount();
            }
        }

        for (StatisticheCalculator.ElementoStat elemento : elementos) {
            container.addView(createBarRow(container, elemento, max));
        }
    }

    private View createBarRow(
            LinearLayout parent,
            StatisticheCalculator.ElementoStat elemento,
            int max
    ) {
        LinearLayout container = new LinearLayout(parent.getContext());
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, dp(parent, 4), 0, dp(parent, 8));
        container.setLayoutParams(containerParams);

        LinearLayout textRow = new LinearLayout(parent.getContext());
        textRow.setOrientation(LinearLayout.HORIZONTAL);
        textRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(parent.getContext());
        name.setText(elemento.getNome());
        name.setTextSize(14);
        name.setTextColor(0xFF333333);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        name.setLayoutParams(nameParams);

        TextView count = new TextView(parent.getContext());
        count.setText(String.valueOf(elemento.getCount()));
        count.setTextSize(14);
        count.setTypeface(Typeface.DEFAULT_BOLD);
        count.setTextColor(0xFF333333);

        textRow.addView(name);
        textRow.addView(count);

        ProgressBar progressBar = new ProgressBar(
                parent.getContext(),
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        progressBar.setMax(max <= 0 ? 1 : max);
        progressBar.setProgress(elemento.getCount());

        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(parent, 8)
        );
        progressParams.setMargins(0, dp(parent, 4), 0, 0);
        progressBar.setLayoutParams(progressParams);

        container.addView(textRow);
        container.addView(progressBar);

        return container;
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FaseViewHolder extends RecyclerView.ViewHolder {
        final TextView faseTitle;
        final TextView faseSubtitle;
        final LinearLayout sintomiContainer;
        final LinearLayout moodContainer;

        public FaseViewHolder(@NonNull View itemView) {
            super(itemView);

            faseTitle = itemView.findViewById(R.id.faseTitle);
            faseSubtitle = itemView.findViewById(R.id.faseSubtitle);
            sintomiContainer = itemView.findViewById(R.id.sintomiContainer);
            moodContainer = itemView.findViewById(R.id.moodContainer);
        }
    }
}