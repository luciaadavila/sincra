package com.example.sincra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.Registrazione;

import java.util.List;

public class GiorniCicloAdapter extends RecyclerView.Adapter<GiorniCicloAdapter.GiorniCicloViewHolder> {
    private List<Registrazione> items;

    public GiorniCicloAdapter(List<Registrazione> items) {
        this.items = items;
    }

    public static class GiorniCicloViewHolder extends RecyclerView.ViewHolder {
        TextView dayCircle;

        public GiorniCicloViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCircle = itemView.findViewById(R.id.dayCircle);
        }
    }

    @Override
    public GiorniCicloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ciclo_giorni, parent, false);

        return new GiorniCicloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiorniCicloViewHolder holder, int position) {
        Registrazione item = items.get(position);

        if (item.isPeriodo()){
            holder.dayCircle.setBackgroundColor(Color.RED);
        } else {
            holder.dayCircle.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

}
