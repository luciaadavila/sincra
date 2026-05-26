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

import java.util.List;

public class CicloAdapter extends RecyclerView.Adapter<CicloAdapter.CicloViewHolder>{
    private List<Ciclo> items;

    public CicloAdapter(List<Ciclo> items) {
        this.items = items;
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

    @NonNull
    @Override
    public CicloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ciclo, parent, false);
        return new CicloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CicloViewHolder holder, int position) {
        Ciclo item = items.get(position);
        holder.title.setText(item.getDurataTotale() + " giorni: " + item.getDataInizio() + " - " + item.getDataFine());
        holder.subtitle.setText("Periodo de " + item.getDurataPeriodo() + " giorni");
        
        holder.daysRecycler.setLayoutManager(
                new LinearLayoutManager(
                        holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        GiorniCicloAdapter adapter = new GiorniCicloAdapter(item.getRegistrazioni());
        holder.daysRecycler.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
