package com.example.sincra.ui;

import static com.example.sincra.database.repositorio.CicloRepository.truncarFecha;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private boolean primoScroll = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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

        // 2. configuramos adapter con el listener de navegación
        adapter = new RegistroAdapter(new ArrayList<>(), item -> {
            DetailDayFragment detailFragment = new DetailDayFragment();
            Bundle bundle = new Bundle();
            bundle.putString("date", dateFormat.format(item.registrazione.getDate()));
            detailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
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
