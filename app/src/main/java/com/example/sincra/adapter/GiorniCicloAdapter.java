package com.example.sincra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.Ciclo;

import java.util.Calendar;

public class GiorniCicloAdapter extends RecyclerView.Adapter<GiorniCicloAdapter.GiorniCicloViewHolder> {

    private final Ciclo ciclo;

    public GiorniCicloAdapter(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    public static class GiorniCicloViewHolder extends RecyclerView.ViewHolder {
        final TextView dayCircle;

        public GiorniCicloViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCircle = itemView.findViewById(R.id.dayCircle);
        }
    }

    @NonNull
    @Override
    public GiorniCicloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ciclo_giorni, parent, false);

        return new GiorniCicloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiorniCicloViewHolder holder, int position) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(ciclo.getDataInizio());
        cal.add(Calendar.DAY_OF_MONTH, position);

        int numeroDia = cal.get(Calendar.DAY_OF_MONTH);
        holder.dayCircle.setText(String.valueOf(numeroDia));

        boolean isPeriodo = position < ciclo.getDurataPeriodo();

        if (isPeriodo) {
            holder.dayCircle.setBackgroundResource(R.drawable.bg_day_period);
            holder.dayCircle.setTextColor(Color.WHITE);
        } else {
            holder.dayCircle.setBackgroundResource(R.drawable.bg_day_normal);
            holder.dayCircle.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    public int getItemCount() {
        if (ciclo == null) return 0;
        return ciclo.getDurataTotale();
    }
}