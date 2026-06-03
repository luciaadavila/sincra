package com.example.sincra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sincra.model.Registrazione;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressRegister;
    private final java.util.concurrent.ExecutorService databaseExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressRegister = findViewById(R.id.progressRegister);


        btnRegister.setOnClickListener(v -> {
            validateAndRegister();
        });

        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> {
            finish();
        });
    }

    private void validateAndRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etEmail.setError("Introduce un email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Introduce una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        registerUser(email, password);
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso, pasamos al flujo principal
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Registro fallido: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // Sincronizar con base de datos local
            databaseExecutor.execute(() -> {
                com.example.sincra.database.AppDatabase db = com.example.sincra.database.AppDatabase.getDatabase(this);
                com.example.sincra.model.User user = db.userDAO().getByFirebaseUid(firebaseUser.getUid());
                long localId;
                if (user == null) {
                    user = new com.example.sincra.model.User(firebaseUser.getUid(),
                            firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario",
                            new java.util.Date(), 28, 5);
                    db.userDAO().insert(user);
                    localId = db.userDAO().getLocalIdByFirebaseUid(firebaseUser.getUid());
                } else {
                    localId = user.getUserId();
                }

                // Guardar localId en SharedPreferences para acceso rápido
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