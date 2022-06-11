package com.example.liveeasy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.liveeasy.adapters.MedAdapter;
import com.example.liveeasy.dao.MedDAO;
import com.example.liveeasy.databinding.ActivityMainBinding;
import com.example.liveeasy.models.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final MedDAO medDAO = new MedDAO();
    private MedAdapter medAdapter;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        binding.userEmailTextView.setText(firebaseUser.getEmail());

        initProgressDialog();
        initRecyclerView();
        loadData();

        binding.addMedButton.setOnClickListener(view -> toInsertMed());
        binding.logoutButton.setOnClickListener(view -> logout());
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching medicine...");
        progressDialog.setCancelable(false);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(
                this,
                2,
                GridLayoutManager.VERTICAL,
                false
        ));
        medAdapter = new MedAdapter(MainActivity.this, medDAO);
        recyclerView.setAdapter(medAdapter);
    }

    private void loadData() {
        progressDialog.show();
        medDAO.get().addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Medicine> temp = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Medicine med = data.getValue(Medicine.class);
                    med.setKey(data.getKey());
                    temp.add(med);
                }
                medAdapter.setListMed(temp);
                medAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void toInsertMed() {
        Intent intent = new Intent(this, InsertMedActivity.class);
        startActivity(intent);
    }

    private void logout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Log out");
        alert.setMessage("Are you sure want to log out?");
        alert.setPositiveButton("Yes", (dialog, id) -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(
                    this,
                    "Successfully logged out!",
                    Toast.LENGTH_LONG
            ).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        alert.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
        alert.show();
    }
}