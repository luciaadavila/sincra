package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;

import java.util.ArrayList;
import java.util.List;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.CatalogoViewHolder> {
    private List<ElementoCatalogo> listaCatalogo;

    public CatalogoAdapter(List<ElementoCatalogo> listaCatalogo) {
        this.listaCatalogo = listaCatalogo;
    }

    @NonNull
    @Override
    public CatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalogo, parent, false);
        return new CatalogoViewHolder(vista);
    }


    @Override
    public void onBindViewHolder(@NonNull CatalogoViewHolder holder, int position) {
        ElementoCatalogo elemento = listaCatalogo.get(position);

        holder.text.setText(elemento.getNome());

        if (elemento.isSelected()) {
            holder.text.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else {
            holder.text.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }
        holder.itemView.setOnClickListener(v -> {
            int actualPosition = holder.getBindingAdapterPosition();
            if (actualPosition != RecyclerView.NO_POSITION) {
                ElementoCatalogo itemClickato = listaCatalogo.get(actualPosition);

                itemClickato.setSelected(!itemClickato.isSelected());

                notifyItemChanged(actualPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCatalogo.size();
    }

    public List<ElementoCatalogo> getSelezionati() {
        List<ElementoCatalogo> selezionati = new ArrayList<>();
        if (listaCatalogo != null) {
            for (ElementoCatalogo e : listaCatalogo) {
                if (e.isSelected()) {
                    selezionati.add(e);
                }
            }
        }
        return selezionati;
    }

    public void updateList(List<ElementoCatalogo> data){
        this.listaCatalogo = data;
        notifyDataSetChanged();
    }



    public static class CatalogoViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;

        public CatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_catalogo);
        }
    }
}
