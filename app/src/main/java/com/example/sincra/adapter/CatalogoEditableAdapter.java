package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.ElementoCatalogo;

import java.util.List;

public class CatalogoEditableAdapter extends RecyclerView.Adapter<CatalogoEditableAdapter.CatalogoEditableViewHolder> {

    private List<ElementoCatalogo> items;
    private final OnCatalogoClickListener listener;

    public CatalogoEditableAdapter(List<ElementoCatalogo> items, OnCatalogoClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public interface OnCatalogoClickListener {
        void onEdit(ElementoCatalogo item);

    }

    public static class CatalogoEditableViewHolder extends RecyclerView.ViewHolder {

        final TextView nameText;

        public CatalogoEditableViewHolder(View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameText);
        }
    }

    @NonNull
    @Override
    public CatalogoEditableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalogo_editable, parent, false);
        return new CatalogoEditableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CatalogoEditableViewHolder holder, int position) {
        ElementoCatalogo item = items.get(position);
        holder.nameText.setText(item.getNome());

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION){
                listener.onEdit(items.get(pos));
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public ElementoCatalogo getItem(int position) {
        return items.get(position);
    }

    public void updateList(List<ElementoCatalogo> data){
        items = data;
        notifyDataSetChanged();
    }
}

