package com.example.sincra.adapter;

import android.annotation.SuppressLint;
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
    private int posizioneSelezzionata = RecyclerView.NO_POSITION;
    private final OnDateClickListener listener;
    private List<String> fechasConPeriodo;
    private List<String> diasProbables;

    private final SimpleDateFormat numFormat = new SimpleDateFormat("dd", Locale.getDefault());
    private final SimpleDateFormat textFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // Es: "lun", "mar"
    private final SimpleDateFormat keyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

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

    public void setPosizioneSelezionata(int nuovaPosizione) {
        if (nuovaPosizione < 0 || nuovaPosizione >= getItemCount()) return;
        if (nuovaPosizione == posizioneSelezzionata) return;

        int posizionePrecedente = posizioneSelezzionata;
        posizioneSelezzionata = nuovaPosizione;

        if (posizionePrecedente != RecyclerView.NO_POSITION) {
            notifyItemChanged(posizionePrecedente);
        }

        notifyItemChanged(posizioneSelezzionata);
    }

    public void setFechasConPeriodo(List<String> fechasConPeriodo) {
        this.fechasConPeriodo = fechasConPeriodo;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setDiasProbables(List<String> diasProbables){
        this.diasProbables = diasProbables;
        notifyItemRangeChanged(0, getItemCount());
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

        holder.dayLabel.setText(textFormat.format(fecha).toUpperCase());
        holder.dayNum.setText(numFormat.format(fecha));

        String keyFecha = keyFormat.format(fecha);

        holder.dayNum.setBackgroundResource(0);

        boolean isSelected = (position == posizioneSelezzionata);
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
            // giorno normale
            holder.dayNum.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.dayLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }


    }


    public int trovaPosizioneData(Date data){
        if (data == null || listaFechas == null) return RecyclerView.NO_POSITION;

        String chiaveData = keyFormat.format(data);

        for (int i=0; i<listaFechas.size(); i++){
            if (keyFormat.format(listaFechas.get(i)).equals(chiaveData)){
                return i;
            }
        }

        return RecyclerView.NO_POSITION;
    }



    @Override
    public int getItemCount() {
        return listaFechas != null ? listaFechas.size() : 0;
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        final TextView dayLabel;
        final TextView dayNum;
        final GestureDetector gestureDetector;

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
                        adapter.setPosizioneSelezionata(currentPos);
                        externalListener.onDateClick(adapter.listaFechas.get(currentPos));
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTap(@NonNull MotionEvent e) {
                    int currentPos = getBindingAdapterPosition();
                    if (externalListener != null && currentPos != RecyclerView.NO_POSITION) {
                        externalListener.onDateDoubleClick(adapter.listaFechas.get(currentPos));
                    }
                    return true;
                }

                @Override
                public boolean onDown(@NonNull MotionEvent e){
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