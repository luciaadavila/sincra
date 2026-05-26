package com.example.sincra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.GiornoItem;

import java.util.List;

public class GiornoAdapter extends RecyclerView.Adapter<GiornoAdapter.GiornoViewHolder> {
    private List<GiornoItem> days;

    public GiornoAdapter(List<GiornoItem> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public GiornoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_giorno, parent, false);
        return new GiornoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiornoViewHolder holder, int position) {
        GiornoItem day = days.get(position);
        holder.giornoText.setText(String.valueOf(day.getNumGiorno()));

        if (day.isSelected()) {
            holder.giornoText.setBackgroundColor(Color.RED);
            holder.giornoText.setTextColor(Color.WHITE);
        } else {
            holder.giornoText.setBackgroundColor(Color.WHITE);
            holder.giornoText.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            day.setSelected(!day.isSelected());
            notifyItemChanged(position);
        });
    }


    @Override
    public int getItemCount() {
        return days.size();
    }

    static class GiornoViewHolder extends RecyclerView.ViewHolder {
        TextView giornoText;
        public GiornoViewHolder(@NonNull View itemView) {
            super(itemView);
            giornoText = itemView.findViewById(R.id.giornoCircle);;
        }

    }

}
