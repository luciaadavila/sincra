package com.example.sincra.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.ui.DetailDayFragment;

import java.util.ArrayList;
import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {
    private List<RegistrazioneConElementi> items;

    public RegistroAdapter(List<RegistrazioneConElementi> items) {
        this.items = items;
    }

    public static class RegistroViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitle, cycleDay, isCycleDay, isProbCycleDay, moods, symptoms;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);

            dayTitle = itemView.findViewById(R.id.dayTitle);
            cycleDay = itemView.findViewById(R.id.cycleDay);
            isCycleDay = itemView.findViewById(R.id.isCycleDay);
            isProbCycleDay = itemView.findViewById(R.id.isProbCycleDay);
            moods = itemView.findViewById(R.id.moods);
            symptoms = itemView.findViewById(R.id.symptoms);
        }
    }

    @Override
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
            DetailDayFragment fragment = new DetailDayFragment();
            Bundle bundle = new Bundle();
            bundle.putString("date", registro.getDate().toString());

            fragment.setArguments(bundle);

            ((FragmentActivity) v.getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.dayTitle.setText(registro.getDate().toString());
        holder.cycleDay.setText("Ciclo: " + registro.getGiornoCiclo());
        holder.isCycleDay.setText("Giorno di ciclo? " + (registro.isGiornoProbabile() ? "Si" : "No"));
        holder.moods.setText("Mood: " + moodsList.toString());
        holder.symptoms.setText("Síntomas: " + symptomsList.toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<RegistrazioneConElementi> newItems){
        this.items = newItems;
        notifyDataSetChanged();
    }

}
