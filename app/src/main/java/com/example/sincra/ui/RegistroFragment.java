package com.example.sincra.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.RegistroAdapter;
import com.example.sincra.viewModel.RegistroViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RegistroFragment extends Fragment {

    private RegistroAdapter adapter;
    private RegistroViewModel viewModel;
    private RecyclerView registroRecycler;
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
        registroRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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
            if (data != null) {
                adapter.setRegistrazioni(data);
            }
        });
    }
}
