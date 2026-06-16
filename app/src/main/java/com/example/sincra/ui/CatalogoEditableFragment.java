package com.example.sincra.ui;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.viewModel.CatalogoViewModel;

import java.util.ArrayList;
import java.util.Objects;


public class CatalogoEditableFragment extends Fragment {

    private CatalogoEditableAdapter adapter;
    private CatalogoViewModel viewModel;
    private String tipo;

    public CatalogoEditableFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_catalogo_editable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.titleText);
        EditText input = view.findViewById(R.id.inputNew);
        Button addButton = view.findViewById(R.id.addButton);
        RecyclerView recycler = view.findViewById(R.id.catalogRecycler);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.red));
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    ElementoCatalogo item = adapter.getItem(position);
                    viewModel.deleteItem(item);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX < 0) {
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

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
        tipo = getArguments() != null ? getArguments().getString(getString(R.string.tipo)) : "mood";
        title.setText(Objects.requireNonNull(tipo).equals("mood") ? getString(R.string.stati_d_animo) : getString(R.string.sintomi));

        viewModel = new ViewModelProvider(this).get(CatalogoViewModel.class);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CatalogoEditableAdapter(new ArrayList<>(), e -> mostraDialogModifica(e));

        recycler.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), data -> adapter.updateList(data));

        viewModel.loadByType(tipo);


        addButton.setOnClickListener(v -> addElemento(input));

        input.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                addElemento(input);
                return true;
            }
            return false;
        });
    }

    private void addElemento(@NonNull EditText input) {
        String inputText = input.getText().toString().trim();

        if (!inputText.isEmpty()) {
            viewModel.addItem(inputText, tipo);
            input.setText("");
        }
    }

    private void mostraDialogModifica(@NonNull ElementoCatalogo item) {
        EditText editText = new EditText(requireContext());
        editText.setText(item.getNome());
        editText.setSelection(editText.getText().length());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.modifica)
                .setView(editText)
                .setPositiveButton(R.string.salva, (dialog, which) -> {
                    String nuovoNome = editText.getText().toString().trim();

                    if (!nuovoNome.isEmpty()) {
                        viewModel.updateItem(item, nuovoNome);
                    }
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }
}