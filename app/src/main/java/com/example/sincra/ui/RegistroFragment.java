package com.example.sincra.ui;

import static com.example.sincra.database.repositorio.CicloRepository.truncarFecha;

import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sincra.R;
import com.example.sincra.adapter.RegistroAdapter;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.viewModel.RegistroViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistroFragment extends Fragment {

    private RegistroAdapter adapter;
    private RegistroViewModel viewModel;
    private RecyclerView registroRecycler;
    private View trashDropZone;

    private boolean primoScroll = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public RegistroFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // 1. configuriamo viste
        registroRecycler = view.findViewById(R.id.registroRecycler);
        registroRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        trashDropZone = view.findViewById(R.id.trashDropZone);
        configuraCestino();

        // 2. configuriamo adapter con il listener di navigazione
        adapter = new RegistroAdapter(new ArrayList<>(), item -> {
            DetailDayFragment detailFragment = new DetailDayFragment();
            Bundle bundle = new Bundle();
            bundle.putString("date", dateFormat.format(item.registrazione.getDate()));
            detailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
            },
            (itemView, item) -> {
                mostraCestino();
                iniziaTrascinamento(itemView, item);
            }
        );
        registroRecycler.setAdapter(adapter);

        // 3. inizializziamo viewModel
        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        viewModel.getRegistri().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;
            adapter.setRegistrazioni(data);

            if (!primoScroll && !data.isEmpty()) {
                int posizioneIniziale = trovaPosizionePiuVicinaAggi(data);
                registroRecycler.scrollToPosition(posizioneIniziale);
                primoScroll = true;
            }

        });
    }

    private void iniziaTrascinamento(View itemView, RegistrazioneConElementi item) {
        ClipData data = ClipData.newPlainText("", "");

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(itemView);
        itemView.startDragAndDrop(data, shadowBuilder, item, 0);
    }

    private void configuraCestino() {
        trashDropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setAlpha(0.7f);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    v.setAlpha(1f);
                    return true;

                case DragEvent.ACTION_DROP:
                    v.setAlpha(1f);

                    Object localState = event.getLocalState();

                    if (localState instanceof RegistrazioneConElementi) {
                        RegistrazioneConElementi item = (RegistrazioneConElementi) localState;
                        if (item.registrazione.isPeriodo()){
                            Toast.makeText(getContext(), "Non puoi eliminare un registro con Periodo", Toast.LENGTH_SHORT).show();
                            nascondiCestino();
                            return true;
                        }
                        eliminaRegistro(item);
                        nascondiCestino();
                        Toast.makeText(getContext(), "Registro eliminato", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1f);
                    nascondiCestino();
                    return true;
            }
            return true;
        });
    }

    private void mostraCestino() {
        trashDropZone.setVisibility(View.VISIBLE);
    }

    private void nascondiCestino() {
        trashDropZone.setVisibility(View.GONE);
    }

    private void eliminaRegistro(RegistrazioneConElementi item) {
        viewModel.deleteRegistrazione(item.registrazione);
    }

    private int trovaPosizionePiuVicinaAggi(List<RegistrazioneConElementi> items) {
        Date oggi = truncarFecha(new Date());

        int migliorPosizione = 0;
        long migliorDistanza = Long.MAX_VALUE;

        for (int i = 0; i < items.size(); i++) {
            Date dataItem = items.get(i).registrazione.getDate();

            if (dataItem == null) continue;

            Date dataItemTroncata = truncarFecha(dataItem);

            long distanza = Math.abs(dataItemTroncata.getTime() - oggi.getTime());

            if (distanza < migliorDistanza) {
                migliorDistanza = distanza;
                migliorPosizione = i;
            }
        }
        return migliorPosizione;
    }
}
