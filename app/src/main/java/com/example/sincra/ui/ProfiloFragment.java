package com.example.sincra.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.sincra.R;
import com.example.sincra.model.User;
import com.example.sincra.viewModel.ProfiloViewModel;
import com.google.android.material.textfield.TextInputEditText;


public class ProfiloFragment extends Fragment {
    private ProfiloViewModel viewModel;
    private User user;

    public ProfiloFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profilo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState){
        super.onViewCreated(view, saveInstanceState);

        TextInputEditText name = view.findViewById(R.id.etName);
        TextInputEditText cycle = view.findViewById(R.id.etCycle);
        TextInputEditText period = view.findViewById(R.id.etPeriod);
        Button saveButton = view.findViewById(R.id.btnSaveProfile);
        Button ricalcolareButton = view.findViewById(R.id.btnRicalcolareDati);

        viewModel = new ViewModelProvider(this).get(ProfiloViewModel.class);
        viewModel.getUserProfilo().observe(getViewLifecycleOwner(), userFromDb -> {
            if (userFromDb != null) {
                this.user = userFromDb;
                // Cada vez que el usuario cambie en la BD, se actualizará la UI automáticamente
                name.setText(userFromDb.getNombre());
                cycle.setText(String.valueOf(userFromDb.getDurataMediaCiclo()));
                period.setText(String.valueOf(userFromDb.getDurataMediaPeriodo()));
            }
        });

        ricalcolareButton.setOnClickListener(v -> {
            v.setEnabled(false);
            viewModel.updateDurataMedia();
            Toast.makeText(getContext(), "Recalculando medias...", Toast.LENGTH_SHORT).show();
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        saveButton.setOnClickListener(v -> {
            if (user == null) return;

            String nameInput = name.getText().toString().trim();
            String cycleInput = cycle.getText().toString().trim();
            String periodInput = period.getText().toString().trim();

            User userUpdated = new User(user);
            if (!nameInput.isEmpty()) userUpdated.setNombre(nameInput);
            if (!cycleInput.isEmpty()) userUpdated.setDurataMediaCiclo(Integer.parseInt(cycleInput));
            if (!periodInput.isEmpty()) userUpdated.setDurataMediaPeriodo(Integer.parseInt(periodInput));
            viewModel.updateUserProfilo(userUpdated);
        });
    }


}