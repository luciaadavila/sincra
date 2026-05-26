package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.InfoAdapter;
import com.example.sincra.model.InfoOption;

import java.util.ArrayList;
import java.util.List;


public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        RecyclerView recycler = view.findViewById(R.id.infoRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        List<InfoOption> options = new ArrayList<>();
        options.add(new InfoOption("Perfil"));
        options.add(new InfoOption("Stato d'animo"));
        options.add(new InfoOption("Sintomi"));
        options.add(new InfoOption("Configuración"));

        InfoAdapter adapter = new InfoAdapter(options);
        recycler.setAdapter(adapter);

        return view;
    }
}
