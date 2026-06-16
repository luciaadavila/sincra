package com.example.sincra;
// codice di autenticazione con Google da https://firebase.google.com/docs/auth/android/google-signin?hl=es-419#java
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

        // Logica Login Email
        findViewById(R.id.btnLoginEmail).setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if(!email.isEmpty() && !password.isEmpty()){
                signInEmail(email, password);
            }
        });

        // Dichiariamo il pulsante
        SignInButton btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGoogleLogin.setOnClickListener(v -> launchCredentialManager());

        findViewById(R.id.tvRegisterLink).setOnClickListener(v -> {
            // Lancia la nuova schermata di Registrazione
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Controlla se l'utente ha effettuato l'accesso (non nullo) e aggiorna l'interfaccia utente di conseguenza.
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
                        Toast.makeText(this, getString(R.string.errore_generico, Objects.requireNonNull(task.getException()).getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchCredentialManager() {
        // configura la richiesta di google
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Impostato su false per mostrare tutti gli account
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        // Crea la richiesta di Credential Manager
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Avvia l'interfaccia utente di Credential Manager
        credentialManager.getCredentialAsync(
                AuthActivity.this,
                request,
                new CancellationSignal(),
                databaseExecutor,
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Estrae le credenziali dal risultato restituito da Credential Manager
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        String errorMsg = getString(R.string.errore_generico, e.getMessage());
                        if (e.getMessage() != null && e.getMessage().contains(getString(R.string.non_si_sono_credenziali_disponibili))) {
                            errorMsg = getString(R.string.errore_google_auth);
                        }
                        final String finalErrorMsg = errorMsg;
                        runOnUiThread(() -> Toast.makeText(AuthActivity.this, finalErrorMsg, Toast.LENGTH_LONG).show());
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        // Controlla se le credenziali sono di tipo Google ID
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Crea il Token ID Google
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Accedi a Firebase usando il token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, getString(R.string.le_credenziali_non_sono_di_tipo_google_id));
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Accesso riuscito, aggiorna l'interfaccia utente con le informazioni dell'utente che ha effettuato l'accesso
                        Log.d(TAG, "signInWithCredential: success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // Se l'accesso fallisce, visualizza un messaggio all'utente
                        Log.w(TAG, "signInWithCredential: failure", task.getException());
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null){
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
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", localId);
                    startActivity(intent);
                    finish();
                });
            });
        }
    }
}