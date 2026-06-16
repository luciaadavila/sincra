package com.example.sincra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.PredictSettimana;

import java.util.ArrayList;
import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder> {
    private List<PredictSettimana> items;

    public PredictionAdapter(List<PredictSettimana> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prediction, parent, false);

        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {

        PredictSettimana item = items.get(position);
        holder.rangeText.setText(item.getRango());
        holder.daysContainer.removeAllViews();

        for (int i = 0; i < item.getPeriodo().size(); i++) {

            boolean isPeriod = item.getPeriodo().get(i);
            int number = item.getNumbers().get(i);

            TextView dayCircle = (TextView) LayoutInflater
                    .from(holder.daysContainer.getContext())
                    .inflate(R.layout.item_giorno_circle_predict, holder.daysContainer, false);

            dayCircle.setText(String.valueOf(number));

            if (isPeriod) {
                dayCircle.setBackgroundResource(R.drawable.bg_day_period);
                dayCircle.setTextColor(Color.WHITE);
            } else {
                dayCircle.setBackgroundResource(R.drawable.bg_day_normal);
                dayCircle.setTextColor(Color.DKGRAY);
            }

            holder.daysContainer.addView(dayCircle);
        }
    }

    public void updateList(List<PredictSettimana> newItems){
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PredictionViewHolder extends RecyclerView.ViewHolder {
        final TextView rangeText;
        final GridLayout daysContainer;

        public PredictionViewHolder(View itemView) {
            super(itemView);

            rangeText = itemView.findViewById(R.id.rangeText);
            daysContainer = itemView.findViewById(R.id.daysContainer);
        }
    }

}
