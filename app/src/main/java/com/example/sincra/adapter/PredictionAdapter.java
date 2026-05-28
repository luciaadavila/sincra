package com.example.sincra.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.PredictSettimana;

import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder> {
    private List<PredictSettimana> items;

    public PredictionAdapter(List<PredictSettimana> items) {
        this.items = items;
    }


    public static class PredictionViewHolder extends RecyclerView.ViewHolder {
        TextView rangeText;
        LinearLayout daysContainer;

        public PredictionViewHolder(View itemView) {
            super(itemView);

            rangeText = itemView.findViewById(R.id.rangeText);
            daysContainer = itemView.findViewById(R.id.daysContainer);
        }
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

            TextView dayCircle = new TextView(holder.itemView.getContext());
            dayCircle.setText(String.valueOf(number));

            dayCircle.setPadding(30, 30, 30, 30);
            GradientDrawable shape = new GradientDrawable();

            shape.setShape(GradientDrawable.OVAL);

            shape.setColor(
                    isPeriod
                            ? Color.parseColor("#E91E63")
                            : Color.LTGRAY
            );

            dayCircle.setBackground(shape);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(12, 0, 12, 0);

            dayCircle.setLayoutParams(params);

            holder.daysContainer.addView(dayCircle);
        }
    }

    public void updateList(List<PredictSettimana> newItems){
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
