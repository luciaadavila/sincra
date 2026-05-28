package com.example.sincra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoAdapter;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.viewModel.DetailDayViewModel;

import java.util.ArrayList;
import java.util.List;

public class DetailDayFragment extends Fragment {

    private CatalogoAdapter moodAdapter;
    private CatalogoAdapter symptomAdapter;
    private DetailDayViewModel viewModel;

    public DetailDayFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_day, container, false);

        RecyclerView moodRecycler = view.findViewById(R.id.moodRecycler);
        RecyclerView symptomRecycler = view.findViewById(R.id.symptomRecycler);

        moodRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        symptomRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        moodAdapter = new CatalogoAdapter(new ArrayList<>());
        symptomAdapter = new CatalogoAdapter(new ArrayList<>());

        moodRecycler.setAdapter(moodAdapter);
        symptomRecycler.setAdapter(symptomAdapter);

        viewModel = new ViewModelProvider(this)
                .get(DetailDayViewModel.class);

        viewModel.getRegistro().observe(getViewLifecycleOwner(), data -> {

            if (data == null) return;

            List<ElementoCatalogo> moods = new ArrayList<>();
            List<ElementoCatalogo> symptoms = new ArrayList<>();

            for (ElementoCatalogo e : data.elementiCatalogo) {

                if ("mood".equals(e.getTipo())) {
                    moods.add(e);
                } else if ("symptom".equals(e.getTipo())) {
                    symptoms.add(e);
                }
            }

            moodAdapter.updateList(moods);
            symptomAdapter.updateList(symptoms);
        });

        String date = getArguments() != null
                ? getArguments().getString("date")
                : null;

        if (date != null) {
            viewModel.loadByDate(date);
        }

        return view;
    }
}