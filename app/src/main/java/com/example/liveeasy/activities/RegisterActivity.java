package com.example.liveeasy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.example.liveeasy.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        initProgressDialog();

        binding.toLoginTextView.setOnClickListener(view -> finish());
        binding.registerButton.setOnClickListener(view -> register());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
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

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Creating your account...");
        progressDialog.setCancelable(false);


    }

    private String validateForm(String email, String password, String passwordConfirm) {
        if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty())
            return "Please fill out all required fields";

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "The entered email is not valid";

        if (!password.equals(passwordConfirm))
            return "Password and confirm password must be the same";

        if (password.length() < 6)
            return "Password must have at least 6 characters long";

        return "";
    }

    private void register() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String passwordConfirm = binding.confirmPasswordEditText.getText().toString();

        String validationErrMsg = validateForm(email, password, passwordConfirm);
        if (!validationErrMsg.isEmpty()) {
            Toast.makeText(this, validationErrMsg, Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener(this, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Exception ex = task.getException();
                if (ex instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Email has already been taken",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Failed to register: " + ex.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
                progressDialog.dismiss();
                return;
            }
            Toast.makeText(
                    getApplicationContext(),
                    "Successfully registered!",
                    Toast.LENGTH_LONG
            ).show();
            startActivity(
                    new Intent(getApplicationContext(), MainActivity.class)
            );
            progressDialog.dismiss();
        });
    }
}
