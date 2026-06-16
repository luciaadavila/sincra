package com.example.sincra.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.utils.FaseCiclo;
import com.example.sincra.utils.FaseCicloUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {
    private List<RegistrazioneConElementi> items;
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longListener;
    private final SimpleDateFormat titleDateFormat = new SimpleDateFormat("EEEE dd MMM yyyy", Locale.ITALIAN);
    private Map<Integer, Ciclo> cicliPerId = new HashMap<>();


    public interface OnItemClickListener {
        void onItemClick(RegistrazioneConElementi item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, RegistrazioneConElementi item);
    }

    public RegistroAdapter(List<RegistrazioneConElementi> items, OnItemClickListener listener, OnItemLongClickListener longListener) {
        this.items = items;
        this.listener = listener;
        this.longListener = longListener;

    }

    @Override
    @NonNull
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro_giorno, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        RegistrazioneConElementi item = items.get(position);
        Registrazione registro = item.registrazione;

        List<String> moodsList = new ArrayList<>();
        List<String> symptomsList = new ArrayList<>();

        for (ElementoCatalogo e : item.elementiCatalogo) {
            if ("mood".equals(e.getTipo())) {
                moodsList.add(e.getNome());
            } else if ("symptom".equals(e.getTipo())) {
                symptomsList.add(e.getNome());
            }
        }

        /*
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });*/

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null){
                longListener.onItemLongClick(v, item);
            }
            return true;
        });

        addSpreadGesture(holder, item);

        holder.dayTitle.setText(titleDateFormat.format(registro.getDate()));

        holder.cycleDay.setText(holder.itemView.getContext().getString(
                        R.string.ciclo_giorno,
                        registro.getGiornoCiclo()
                ));

        FaseCiclo fase = configuraFaseCiclo(registro);

        if (fase != null) {
            holder.phase.setText(holder.itemView.getContext().getString(R.string.fase_label, holder.itemView.getContext().getString(fase.getResId())));
        } else {
            holder.phase.setText(R.string.fase_vuota);
        }

        String isCycle = holder.itemView.getContext().getString(registro.isPeriodo() ? R.string.si : R.string.no);
        holder.isCycleDay.setText(holder.itemView.getContext().getString(R.string.is_ciclo_giorno, isCycle));

        int numPassi = registro.getPassi();
        if (numPassi >= 0){
            holder.steps.setText(holder.itemView.getContext().getString(R.string.passi_label, numPassi));
        } else {
            holder.steps.setText(R.string.passi_non_disponibili);
        }

        String moodsText = moodsList.isEmpty()
                ? "—"
                : TextUtils.join(", ", moodsList);

        String symptomsText = symptomsList.isEmpty()
                ? "—"
                : TextUtils.join(", ", symptomsList);

        holder.moods.setText(holder.itemView.getContext().getString(R.string.umore_label, moodsText));
        holder.symptoms.setText(holder.itemView.getContext().getString(R.string.sintomi_label, symptomsText));
    }

    private FaseCiclo configuraFaseCiclo(Registrazione registro){
        Ciclo ciclo = cicliPerId.get(registro.getCicloId());
        int durataPeriodo = 5;
        int durataTotale = 28;

        if (ciclo != null) {
            if (ciclo.getDurataPeriodo() > 0){
                durataPeriodo = ciclo.getDurataPeriodo();
            }
            if (ciclo.getDurataTotale() > 0){
                durataTotale = ciclo.getDurataTotale();
            }
        }

        return FaseCicloUtils.calcoloFase(registro.getGiornoCiclo(), durataPeriodo, durataTotale);
    }

    private void addSpreadGesture(RegistroViewHolder holder, RegistrazioneConElementi item){
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private float scale = 1f;
            private boolean spreadDetectado = false;
            private boolean gestoMultitouch = false;

            private final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(
                    holder.itemView.getContext(),
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(@NonNull ScaleGestureDetector detector) {
                            scale *= detector.getScaleFactor();
                            if (scale > 1.15f) {
                                spreadDetectado = true;
                                holder.itemView.animate().scaleX(1.04f).scaleY(1.04f).setDuration(80).start();
                            }
                            return true;
                        }
                    }
            );

            @Override
            public boolean onTouch(View v, MotionEvent event){
                scaleDetector.onTouchEvent(event);
                if (event.getPointerCount()>1){
                    gestoMultitouch = true;

                    v.cancelLongPress();
                    v.setLongClickable(false);

                    // per evitar il scroll verticale
                    if (v.getParent() != null){
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();

                    if (v.getParent() != null){
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }

                    if (spreadDetectado && listener != null) {
                        listener.onItemClick(item);
                    }

                    boolean consumirEvento = gestoMultitouch;

                    scale = 1f;
                    spreadDetectado = false;
                    gestoMultitouch = false;
                    v.setLongClickable(true);

                    return consumirEvento;
                }
                return gestoMultitouch;
            }
        });
    }

    public void setRegistrazioni(List<RegistrazioneConElementi> newItems){
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setCicliPerId(Map<Integer, Ciclo> cicliPerId) {
        this.cicliPerId = cicliPerId != null ? cicliPerId : new HashMap<>();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class RegistroViewHolder extends RecyclerView.ViewHolder {
        final TextView dayTitle;
        final TextView cycleDay;
        final TextView isCycleDay;
        final TextView moods;
        final TextView symptoms;
        final TextView phase;
        final TextView steps;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            cycleDay = itemView.findViewById(R.id.cycleDay);
            steps = itemView.findViewById(R.id.steps);
            isCycleDay = itemView.findViewById(R.id.isCycleDay);
            moods = itemView.findViewById(R.id.moods);
            symptoms = itemView.findViewById(R.id.symptoms);
            phase = itemView.findViewById(R.id.phase);
        }
    }

}
