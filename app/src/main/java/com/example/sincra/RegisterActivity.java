package com.example.sincra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sincra.model.Registrazione;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
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
            Executors.newSingleThreadExecutor().execute(() -> {
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