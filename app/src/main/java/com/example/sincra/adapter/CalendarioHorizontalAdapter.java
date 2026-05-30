package com.example.sincra.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
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
    private int posicionSeleccionada = 15; // Empezamos enfocados en "Hoy" (el centro de nuestra lista)
    private OnDateClickListener listener;
    private List<String> fechasConPeriodo; // Formato "yyyy-MM-dd" que traeremos de Room


    private long ultimoClick = 0;
    private int ultimoClickPosicion = -1;
    private static final long INTERVALO = 500;


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

    public void setFechasConPeriodo(List<String> fechasConPeriodo) {
        this.fechasConPeriodo = fechasConPeriodo;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_giorno, parent, false);
        return new CalendarViewHolder(view);
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

        // LOGICA DE COLORES (REGLA VS NORMAL VS SELECCIONADO)
        if (actualPosition == posicionSeleccionada) {
            // Está seleccionado por el usuario
            holder.dayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.dayLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else if (fechasConPeriodo != null && fechasConPeriodo.contains(keyFecha)) {
            // Este día tiene marcado período (REGLA EN ROJO)
            holder.dayNum.setTextColor(Color.RED);
            holder.dayLabel.setTextColor(Color.RED);
            holder.dayNum.setBackgroundResource(R.drawable.circle_red_border);

        } else {
            // Día normal
            holder.dayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.dayLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }

        holder.itemView.setOnClickListener(v -> {
            long tiempoActual = System.currentTimeMillis();
            int currentPos = holder.getBindingAdapterPosition();

            if (currentPos == ultimoClickPosicion && (tiempoActual - ultimoClick < INTERVALO)){
                if (listener != null && currentPos != RecyclerView.NO_POSITION) {
                    listener.onDateDoubleClick(listaFechas.get(currentPos));
                }
                // Reset para evitar triple click
                ultimoClick = 0;
                ultimoClickPosicion = -1;
            } else {
                int prevPos = posicionSeleccionada;
                posicionSeleccionada = currentPos;
                notifyItemChanged(prevPos);
                notifyItemChanged(posicionSeleccionada);

                if (listener != null && posicionSeleccionada != RecyclerView.NO_POSITION) {
                    listener.onDateClick(listaFechas.get(posicionSeleccionada));
                }
                ultimoClick = tiempoActual;
                ultimoClickPosicion = currentPos;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFechas.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dayLabel, dayNum;
        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.dayLabelText);
            dayNum = itemView.findViewById(R.id.dayNumberText);
        }
    }
}