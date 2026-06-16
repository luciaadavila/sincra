package com.example.sincra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private final java.util.concurrent.ExecutorService databaseExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> validateAndRegister());

        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> finish());
    }

    private void validateAndRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.inserisci_email));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.email_non_valida));
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.inserisci_password));
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_corta));
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_non_corrispondono));
            return;
        }

        registerUser(email, password);
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registrazione riuscita, passiamo al flusso principale
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, getString(R.string.registrazione_fallita, Objects.requireNonNull(task.getException()).getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // sincronizza con il database locale
            databaseExecutor.execute(() -> {
                com.example.sincra.database.AppDatabase db = com.example.sincra.database.AppDatabase.getDatabase(this);
                com.example.sincra.model.User user = db.userDAO().getByFirebaseUidSync(firebaseUser.getUid());
                long localId;
                if (user == null) {
                    user = new com.example.sincra.model.User(firebaseUser.getUid(),
                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : getString(R.string.utente),
                            new java.util.Date(), 28, 5);
                    db.userDAO().insert(user);
                    localId = db.userDAO().getLocalIdByFirebaseUidSync(firebaseUser.getUid());
                } else {
                    localId = user.getUserId();
                }

                // salva localId in SharedPreferences per un accesso rapido
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
                        .putLong("local_user_id", localId).apply();

                runOnUiThread(() -> {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", localId);
                    startActivity(intent);
                    finish();
                });
            });
        }
    }
}