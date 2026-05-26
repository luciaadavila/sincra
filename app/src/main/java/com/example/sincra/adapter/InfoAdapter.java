package com.example.sincra.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.InfoOption;

import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {
    private List<InfoOption> options;

    public InfoAdapter(List<InfoOption> options) {
        this.options = options;
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView optionTitle;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            optionTitle = itemView.findViewById(R.id.optionTitle);
        }
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info_option, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        InfoOption option = options.get(position);
        holder.optionTitle.setText(option.getTitle());
        holder.itemView.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

}
