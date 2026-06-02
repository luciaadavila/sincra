package com.example.sincra.adapter;

import android.location.GnssAntennaInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

public class CatalogoEditableAdapter extends RecyclerView.Adapter<CatalogoEditableAdapter.CatalogoEditableViewHolder> {

    private List<ElementoCatalogo> items;
    private OnCatalogoClickListener listener;

    public CatalogoEditableAdapter(List<ElementoCatalogo> items, OnCatalogoClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public interface OnCatalogoClickListener {
        void onEdit(ElementoCatalogo item);
        void onDelete(ElementoCatalogo item);
    }

    public static class CatalogoEditableViewHolder extends RecyclerView.ViewHolder {

        TextView nameText;
        Button editButton, deleteButton;

        public CatalogoEditableViewHolder(View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @Override
    public CatalogoEditableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_catalogo_editable,
                        parent,
                        false);

        return new CatalogoEditableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CatalogoEditableViewHolder holder, int position) {

        ElementoCatalogo item = items.get(position);

        holder.nameText.setText(item.getNome());
        holder.deleteButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                listener.onDelete(items.get(pos));
            }
        });

        holder.editButton.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                listener.onEdit(items.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<ElementoCatalogo> data){
        items = data;
        notifyDataSetChanged();
    }
}

