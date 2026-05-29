package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;

import java.util.ArrayList;
import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {
    private List<RegistrazioneConElementi> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RegistrazioneConElementi item);
    }

    public RegistroAdapter(List<RegistrazioneConElementi> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class RegistroViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitle, cycleDay, isCycleDay, moods, symptoms;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            cycleDay = itemView.findViewById(R.id.cycleDay);
            isCycleDay = itemView.findViewById(R.id.isCycleDay);
            moods = itemView.findViewById(R.id.moods);
            symptoms = itemView.findViewById(R.id.symptoms);
        }
    }

    @Override
    @NonNull
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro_giorno, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        RegistrazioneConElementi item = items.get(position);
        Registrazione registro = item.registrazione;
        List<String> moodsList = new ArrayList<>();
        List<String> symptomsList = new ArrayList<>();

        for (ElementoCatalogo e : item.elementiCatalogo) {
            if ("mood".equals(e.getTipo())) {
                moodsList.add(e.getNome());
            } else if ("symptom".equals(e.getTipo())) {
                symptomsList.add(e.getNome());
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.dayTitle.setText(registro.getDate().toString());
        
        holder.cycleDay.setText(holder.itemView.getContext().getString(R.string.ciclo_giorno, registro.getGiornoCiclo()));
        
        String isCycle = holder.itemView.getContext().getString(registro.isGiornoProbabile() ? R.string.si : R.string.no);
        holder.isCycleDay.setText(holder.itemView.getContext().getString(R.string.is_ciclo_giorno, isCycle));
        
        holder.moods.setText(holder.itemView.getContext().getString(R.string.mood_label, moodsList.toString()));
        holder.symptoms.setText(holder.itemView.getContext().getString(R.string.sintomi_label, symptomsList.toString()));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateList(List<RegistrazioneConElementi> newItems){
        this.items = newItems;
        notifyDataSetChanged();
    }
}
