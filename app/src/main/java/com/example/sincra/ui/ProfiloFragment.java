package com.example.sincra.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.sincra.AuthActivity;
import com.example.sincra.R;
import com.example.sincra.model.User;
import com.example.sincra.viewModel.ProfiloViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.concurrent.Executors;


public class ProfiloFragment extends Fragment {
    private ProfiloViewModel viewModel;
    private User user;

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;



    public ProfiloFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());

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
        Button logoutButton = view.findViewById(R.id.btnLogout);

        viewModel = new ViewModelProvider(this).get(ProfiloViewModel.class);
        viewModel.getUserProfilo().observe(getViewLifecycleOwner(), userFromDb -> {
            if (userFromDb != null) {
                this.user = userFromDb;
                // Ogni volta che l'utente cambia nel DB, la UI si aggiornerà automaticamente
                name.setText(userFromDb.getNome());
                cycle.setText(String.valueOf(userFromDb.getDurataMediaCiclo()));
                period.setText(String.valueOf(userFromDb.getDurataMediaPeriodo()));
            }
        });

        ricalcolareButton.setOnClickListener(v -> {
            v.setEnabled(false);
            viewModel.updateDurataMedia();
            Toast.makeText(getContext(), "Ricalcolando le medie...", Toast.LENGTH_SHORT).show();
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });

        saveButton.setOnClickListener(v -> {
            if (user == null) return;

            String nameInput = Objects.requireNonNull(name.getText()).toString().trim();
            String cycleInput = Objects.requireNonNull(cycle.getText()).toString().trim();
            String periodInput = Objects.requireNonNull(period.getText()).toString().trim();

            User userUpdated = new User(user);
            if (!nameInput.isEmpty()) userUpdated.setNome(nameInput);
            if (!cycleInput.isEmpty()) userUpdated.setDurataMediaCiclo(Integer.parseInt(cycleInput));
            if (!periodInput.isEmpty()) userUpdated.setDurataMediaPeriodo(Integer.parseInt(periodInput));
            viewModel.updateUserProfilo(userUpdated);
        });

        logoutButton.setOnClickListener(v -> signOut());


    }

    // metodo per uscire dall'account
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
            clearRequest,
            new CancellationSignal(),
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<>() {
                @Override
                public void onResult(@NonNull Void result) {
                    requireActivity().runOnUiThread(() -> goToLogin());
                }

                @Override
                public void onError(@NonNull ClearCredentialException e) {
                    requireActivity().runOnUiThread(() -> goToLogin());
                }
            });
    }

    private void goToLogin() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}