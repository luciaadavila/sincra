package com.example.sincra;
// codice di autenticazione con Google di https://firebase.google.com/docs/auth/android/google-signin?hl=es-419#java
// https://github.com/firebase/snippets-android/blob/a413b0658ff2fc7a72c4b0c59e84a889ff7fac45/auth/app/src/main/java/com/google/firebase/quickstart/auth/GoogleSignInActivity.java
// https://github.com/firebase/snippets-android/blob/a413b0658ff2fc7a72c4b0c59e84a889ff7fac45/auth/app/src/main/java/com/google/firebase/quickstart/auth/EmailPasswordActivity.java#L60


import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private EditText etEmail, etPassword;
    private final java.util.concurrent.ExecutorService databaseExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(getBaseContext());

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Lógica Login Email
        findViewById(R.id.btnLoginEmail).setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if(!email.isEmpty() && !password.isEmpty()){
                signInEmail(email, password);
            }
        });

        // Declaramos el botón
        SignInButton btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGoogleLogin.setOnClickListener(v -> launchCredentialManager());

        findViewById(R.id.tvRegisterLink).setOnClickListener(v -> {
            // Lanza tu nueva pantalla de Registro
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            updateUI(currentUser);
        }
    }

    private void signInEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchCredentialManager() {
        // configurar la solicitud de google
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Cambiado a false para mostrar todas las cuentas
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        // Create the Credential Manager request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Launch Credential Manager UI
        credentialManager.getCredentialAsync(
                AuthActivity.this,
                request,
                new CancellationSignal(),
                databaseExecutor,
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Extract credential from the result returned by Credential Manager
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        String errorMsg = "Error: " + e.getMessage();
                        if (e.getMessage() != null && e.getMessage().contains("No credentials available")) {
                            errorMsg = "No hay cuentas de Google vinculadas o error de configuración (SHA-1/Package Name).";
                        }
                        final String finalErrorMsg = errorMsg;
                        runOnUiThread(() -> Toast.makeText(AuthActivity.this, finalErrorMsg, Toast.LENGTH_LONG).show());
                    }
                }
        );
    }

    // [START handle_sign_in]
    private void handleSignIn(Credential credential) {
        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
        }
    }
    // [END handle_sign_in]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }
    // [END auth_with_google]

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null){
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
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", localId);
                    startActivity(intent);
                    finish();
                });
            });
        }
    }
}