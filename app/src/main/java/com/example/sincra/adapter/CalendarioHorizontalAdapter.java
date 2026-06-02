package com.example.sincra.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarioHorizontalAdapter extends RecyclerView.Adapter<CalendarioHorizontalAdapter.CalendarViewHolder> {

    private List<Date> listaFechas;
    private int posicionSeleccionada = -1; 
    private OnDateClickListener listener;
    private List<String> fechasConPeriodo; // Formato "yyyy-MM-dd" que traeremos de Room
    private List<String> diasProbables; // Formato "yyyy-MM-dd" que traeremos de Room

    public interface OnDateClickListener {
        void onDateClick(Date fechaSeleccionada);
        void onDateDoubleClick(Date fechaSeleccionada);
    }

    public CalendarioHorizontalAdapter(List<Date> listaFechas, OnDateClickListener listener) {
        this.listaFechas = listaFechas;
        this.listener = listener;
    }

    public void setListaFechas(List<Date> listaFechas) {
        this.listaFechas = listaFechas;
        notifyDataSetChanged();
    }

    public void setPosicionSeleccionada(int position) {
        this.posicionSeleccionada = position;
        notifyDataSetChanged();
    }

    public void setFechasConPeriodo(List<String> fechasConPeriodo) {
        this.fechasConPeriodo = fechasConPeriodo;
        notifyDataSetChanged();
    }

    public void setDiasProbables(List<String> diasProbables){
        this.diasProbables = diasProbables;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_giorno, parent, false);
        return new CalendarViewHolder(view, listener, this);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Date fecha = listaFechas.get(position);

        SimpleDateFormat numFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat textFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Ej: "lun", "mar"

        holder.dayLabel.setText(textFormat.format(fecha).toUpperCase());
        holder.dayNum.setText(numFormat.format(fecha));

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String keyFecha = keyFormat.format(fecha);

        int actualPosition = holder.getBindingAdapterPosition();

        // Limpiamos el fondo por defecto para evitar errores al reciclar vistas
        holder.dayNum.setBackgroundResource(0);

        boolean isSelected = (actualPosition == posicionSeleccionada);
        boolean isPeriodDay = (fechasConPeriodo != null && fechasConPeriodo.contains(keyFecha));
        boolean isProbableDay = (diasProbables != null && diasProbables.contains(keyFecha));


        if (isPeriodDay){
            holder.dayNum.setBackgroundResource(R.drawable.circle_red_border);
        } else if (isProbableDay){
            holder.dayNum.setBackgroundResource(R.drawable.circle_pink_border);
        }

        if (isSelected) {
            holder.dayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.dayLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));

        } else {
            // Día normal
            holder.dayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.dayLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }


    }

    public void updateSelection(int newPosition){
        int prevPos = posicionSeleccionada;
        posicionSeleccionada = newPosition;
        notifyItemChanged(prevPos);
        notifyItemChanged(posicionSeleccionada);
    }

    @Override
    public int getItemCount() {
        return listaFechas.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dayLabel, dayNum;
        GestureDetector gestureDetector;

        @SuppressLint("ClickableViewAccessibility")
        public CalendarViewHolder(@NonNull View itemView, OnDateClickListener externalListener, CalendarioHorizontalAdapter adapter) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.dayLabelText);
            dayNum = itemView.findViewById(R.id.dayNumberText);

            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                    int currentPos = getBindingAdapterPosition();
                    if (externalListener != null && currentPos != RecyclerView.NO_POSITION) {
                        // CORREGIDO: Añadido "adapter." para solucionar los errores estáticos
                        adapter.updateSelection(currentPos);
                        externalListener.onDateClick(adapter.listaFechas.get(currentPos));
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    int currentPos = getBindingAdapterPosition();
                    if (externalListener != null && currentPos != RecyclerView.NO_POSITION) {
                        // CORREGIDO: Añadido "adapter." para obtener la fecha correctamente
                        externalListener.onDateDoubleClick(adapter.listaFechas.get(currentPos));
                    }
                    return true;
                }
            });

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });
        }
    }
}