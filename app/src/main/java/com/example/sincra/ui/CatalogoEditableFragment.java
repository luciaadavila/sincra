package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoEditableAdapter;
import com.example.sincra.model.ElementoCatalogo;

import java.util.ArrayList;
import java.util.List;


public class CatalogoEditableFragment extends Fragment {

    private List<ElementoCatalogo> items;
    private CatalogoEditableAdapter adapter;

    public CatalogoEditableFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_catalogo_editable, container, false);

        TextView title = view.findViewById(R.id.titleText);
        EditText input = view.findViewById(R.id.inputNew);
        Button addButton = view.findViewById(R.id.addButton);
        RecyclerView recycler = view.findViewById(R.id.catalogRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        items = new ArrayList<>();
        items.add(new ElementoCatalogo("Feliz", "mood"));
        items.add(new ElementoCatalogo("Ansiosa", "mood"));

        adapter = new CatalogoEditableAdapter(items);

        recycler.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                items.add(new ElementoCatalogo(inputText, "mood"));
                adapter.notifyDataSetChanged();
                input.setText("");
            }
        });

        return view;
    }
}