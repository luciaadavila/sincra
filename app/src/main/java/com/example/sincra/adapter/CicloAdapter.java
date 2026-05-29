package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CicloViewHolder>{
    private List<Object> items;
    private final OnCicloClickListener clickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnCicloClickListener {
        void onCicloClick(Object item);
    }

    public CicloAdapter(List<?> items, OnCicloClickListener clickListener) {
        this.items = (List<Object>) items;
        this.clickListener = clickListener;
    }

    public void setList(List<?> nuevosItems) {
        this.items = (List<Object>) nuevosItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CicloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ciclo, parent, false);
        return new CicloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CicloViewHolder holder, int position) {
        Object item = items.get(position);

        Ciclo ciclo = null;
        List<?> registrazioni = new ArrayList<>();

        if (item instanceof CicloConRegistrazioni) {
            CicloConRegistrazioni relacion = (CicloConRegistrazioni) item;
            ciclo = relacion.getCiclo();
            registrazioni = relacion.getRegistrazioni();
        } else if (item instanceof Ciclo) {
            ciclo = (Ciclo) item;
        }

        if (ciclo != null){
            String start = dateFormat.format(ciclo.getDataInizio());
            String end = ciclo.getDataFine() != null ? dateFormat.format(ciclo.getDataFine()) : "...";
            
            holder.title.setText(holder.itemView.getContext().getString(R.string.giorni_ciclo_durata, 
                    ciclo.getDurataTotale(), start, end));
            
            holder.subtitle.setText(holder.itemView.getContext().getString(R.string.periodo_durata, 
                    ciclo.getDurataPeriodo()));

            if (holder.daysRecycler.getLayoutManager() == null){
                holder.daysRecycler.setLayoutManager(
                        new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
                );
            }
        }

        GiorniCicloAdapter adapter = new GiorniCicloAdapter((List) registrazioni);
        holder.daysRecycler.setAdapter(adapter);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && clickListener != null) {
                clickListener.onCicloClick(items.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class CicloViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        RecyclerView daysRecycler;

        public CicloViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            daysRecycler = itemView.findViewById(R.id.daysRecycler);
        }
    }
}
