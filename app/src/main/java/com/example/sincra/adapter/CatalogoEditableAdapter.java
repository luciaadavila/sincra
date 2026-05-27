package com.example.sincra.adapter;

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

    private List<
            ElementoCatalogo> items;

    public CatalogoEditableAdapter(List<ElementoCatalogo> items) {
        this.items = items;
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
            items.remove(position);
            notifyDataSetChanged();
        });

        holder.editButton.setOnClickListener(v -> {

            // luego hacemos popup editar
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}