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
import androidx.recyclerview.widget.LinearSnapHelper;
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

        // 1. configuramos vistas
        registroRecycler = view.findViewById(R.id.registroRecycler);
        registroRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        trashDropZone = view.findViewById(R.id.trashDropZone);
        configurarPapelera();

        // 2. configuramos adapter con el listener de navegación
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
                mostrarPapelera();
                empezarArrastre(itemView, item);
            }
        );
        registroRecycler.setAdapter(adapter);

        // 3. inicializamos viewModel
        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        viewModel.getRegistri().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;
            adapter.setRegistrazioni(data);

            if (!primoScroll && !data.isEmpty()) {
                int posicionInicial = findClosestPositionToToday(data);
                registroRecycler.scrollToPosition(posicionInicial);
                primoScroll = true;
            }

        });
    }

    private void empezarArrastre(View itemView, RegistrazioneConElementi item) {
        ClipData data = ClipData.newPlainText("", "");

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(itemView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            itemView.startDragAndDrop(data, shadowBuilder, item, 0);
        } else {
            itemView.startDrag(data, shadowBuilder, item, 0);
        }
    }

    private void configurarPapelera() {
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
                            Toast.makeText(getContext(), "Non poi eliminare un registro con Periodo", Toast.LENGTH_SHORT).show();
                            ocultarPapelera();
                            return true;
                        }
                        borrarRegistro(item);
                        ocultarPapelera();
                        Toast.makeText(getContext(), "Registro borrado", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    v.setAlpha(1f);
                    ocultarPapelera();
                    return true;
            }
            return true;
        });
    }

    private void mostrarPapelera() {
        trashDropZone.setVisibility(View.VISIBLE);
    }

    private void ocultarPapelera() {
        trashDropZone.setVisibility(View.GONE);
    }

    private void borrarRegistro(RegistrazioneConElementi item) {
        viewModel.deleteRegistrazione(item.registrazione);
    }

    private int findClosestPositionToToday(List<RegistrazioneConElementi> items) {
        Date today = truncarFecha(new Date());

        int bestPosition = 0;
        long bestDistance = Long.MAX_VALUE;

        for (int i = 0; i < items.size(); i++) {
            Date itemDate = items.get(i).registrazione.getDate();

            if (itemDate == null) continue;

            Date itemDateTruncada = truncarFecha(itemDate);

            long distance = Math.abs(itemDateTruncada.getTime() - today.getTime());

            if (distance < bestDistance) {
                bestDistance = distance;
                bestPosition = i;
            }
        }
        return bestPosition;
    }
}
