package com.example.sincra.ui;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoEditableAdapter;
import com.example.sincra.database.AppDatabase;
import com.example.sincra.database.dao.ElementoCatalogoDAO;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.viewModel.CatalogoViewModel;

import java.util.ArrayList;
import java.util.List;


public class CatalogoEditableFragment extends Fragment {

    private CatalogoEditableAdapter adapter;
    private CatalogoViewModel viewModel;
    private String tipo;

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

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.red));
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash);

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    ElementoCatalogo item = adapter.getItem(position);
                    viewModel.deleteItem(item);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX < 0) {
                    // Fondo rojo al deslizar hacia la izquierda
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Icono de papelera
                    if (deleteIcon != null) {
                        int iconMargin = 32;
                        int iconSize = deleteIcon.getIntrinsicWidth();

                        int top = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                        int bottom = top + iconSize;
                        int right = itemView.getRight() - iconMargin;
                        int left = right - iconSize;

                        deleteIcon.setBounds(left, top, right, bottom);
                        deleteIcon.draw(c);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler);
        tipo = getArguments() != null ? getArguments().getString("tipo") : "mood";
        title.setText(tipo.equals("mood") ? "Stati d'animo" : "Sintomi");

        viewModel = new ViewModelProvider(this).get(CatalogoViewModel.class);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CatalogoEditableAdapter(new ArrayList<>(), new CatalogoEditableAdapter.OnCatalogoClickListener(){
            @Override
            public void onEdit(ElementoCatalogo e){
                mostrarDialogEditar(e);
            }
        });

        recycler.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), data -> {
            adapter.updateList(data);
        });

        viewModel.loadByType(tipo);


        addButton.setOnClickListener(v -> {
            addElemento(input);
        });

        input.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                addElemento(input);
                return true;
            }
            return false;
        });


        return view;
    }

    private void addElemento(EditText input) {
        String inputText = input.getText().toString().trim();

        if (!inputText.isEmpty()) {
            viewModel.addItem(inputText, tipo);
            input.setText("");
        }
    }

    private void mostrarDialogEditar(ElementoCatalogo item) {
        EditText editText = new EditText(requireContext());
        editText.setText(item.getNome());
        editText.setSelection(editText.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Modifica")
                .setView(editText)
                .setPositiveButton("Salva", (dialog, which) -> {
                    String nuovoNome = editText.getText().toString().trim();

                    if (!nuovoNome.isEmpty()) {
                        viewModel.updateItem(item, nuovoNome);
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }
}