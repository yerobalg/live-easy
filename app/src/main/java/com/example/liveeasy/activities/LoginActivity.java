package com.example.liveeasy.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.example.liveeasy.R;
import com.example.liveeasy.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        initProgressDialog();
        allowLoginWithGoogle();
        binding.registerTextView.setOnClickListener(view -> startActivity(new Intent(this,
                RegisterActivity.class)));
        binding.loginButton.setOnClickListener(view -> login());
        binding.googleImageView.setOnClickListener(view -> loginGoogle());
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Just a few second...");
        progressDialog.setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(
                    this,
                    String.format("Welcome, %s!", currentUser.getEmail()),
                    Toast.LENGTH_SHORT
            ).show();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private String validateForm(String email, String password) {
        if (email.isEmpty() || password.isEmpty())
            return "Please fill out all required fields";

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "The entered email is not valid";

        return "";
    }

    private void login() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String validationErrMsg = validateForm(email, password);

        if (!validationErrMsg.isEmpty()) {
            Toast.makeText(this, validationErrMsg, Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.show();
        mAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Exception ex = task.getException();
                        if (ex instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(
                                    this,
                                    "The entered email is not found",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                    this,
                                    "Wrong password",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Login failed: " + ex.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        progressDialog.dismiss();
                        return;
                    }
                    Toast.makeText(
                            this,
                            "Login successful!",
                            Toast.LENGTH_LONG
                    ).show();
                    startActivity(new Intent(
                            getApplicationContext(),
                            MainActivity.class
                    ));
                });
    }

    private void allowLoginWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("GOOGLE_LOGIN_ERROR", e.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider
                .getCredential(idToken, null);

        progressDialog.show();
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Login failed: " + task.getException(),
                                Toast.LENGTH_LONG
                        ).show();
                        progressDialog.dismiss();
                        return;
                    }
                    Toast.makeText(
                            this,
                            "Login successful!",
                            Toast.LENGTH_LONG
                    ).show();
                    progressDialog.dismiss();

                    startActivity(new Intent(
                            getApplicationContext(), MainActivity.class
                    ));
                });
    }
}