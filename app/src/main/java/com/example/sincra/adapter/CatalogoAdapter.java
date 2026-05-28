package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;

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

        holder.text.setTextColor(elemento.isSelected() ? 0xFFE91E63 : 0xFF888888);

        holder.itemView.setOnClickListener(v -> {
            elemento.setSelected(!elemento.isSelected());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return listaCatalogo.size();
    }

    public static class CatalogoViewHolder extends RecyclerView.ViewHolder {
        private TextView text;

        public CatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_catalogo);
        }
    }

    public void updateList(List<ElementoCatalogo> data){
        this.listaCatalogo = data;
        notifyDataSetChanged();
    }

}
