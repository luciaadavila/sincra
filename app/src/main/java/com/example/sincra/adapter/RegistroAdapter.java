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
import com.example.sincra.model.RegistroDiario;
import com.example.sincra.ui.DetailDayFragment;

import java.util.List;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {
    private List<RegistroDiario> items;

    public RegistroAdapter(List<RegistroDiario> items) {
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
        RegistroDiario item = items.get(position);


        holder.itemView.setOnClickListener(v -> {

            DetailDayFragment fragment = new DetailDayFragment();
            Bundle bundle = new Bundle();
            bundle.putString("date", item.getDate());

            fragment.setArguments(bundle);

            ((FragmentActivity) v.getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.dayTitle.setText(item.getDate());
        holder.cycleDay.setText("Ciclo: " + item.getGiornoCiclo());
        holder.isCycleDay.setText("Giorno di ciclo? " + (item.isGiornoProbabile() ? "Si" : "No"));
        holder.moods.setText("Mood: " + item.getIdStatoAnimo());
        holder.symptoms.setText("Síntomas: " + item.getIdSintomi());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
