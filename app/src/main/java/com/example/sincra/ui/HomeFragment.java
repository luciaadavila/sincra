package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sincra.R;
import com.example.sincra.adapter.GiornoAdapter;
import com.example.sincra.model.GiornoItem;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView dayRecycler;
    private Button dayButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dayButton = view.findViewById(R.id.dayButton);
        dayButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new DetailDayFragment()).addToBackStack(null).commit();
        });

        dayRecycler = view.findViewById(R.id.dayRecycler);

        dayRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<GiornoItem> days = new ArrayList<>();

        for (int i = 1; i <= 31; i++) {
            days.add(new GiornoItem(i, false));
        }

        GiornoAdapter adapter = new GiornoAdapter(days);

        dayRecycler.setAdapter(adapter);

        return view;
    }
}