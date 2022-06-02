package com.example.liveeasy.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.liveeasy.dao.MedDAO;
import com.example.liveeasy.databinding.LayoutFormBinding;
import com.example.liveeasy.helpers.UploadImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class UpdateMedActivity extends AppCompatActivity {

    private LayoutFormBinding binding;
    private final MedDAO medDao = new MedDAO();
    private ProgressDialog progressDialog;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initProgressDialog();

        binding.uploadButton.setOnClickListener(view -> chooseImage());
        binding.submitButton.setOnClickListener(view -> update());
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Just a few second...");
        progressDialog.setCancelable(false);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Image"), 20
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 20 || resultCode != RESULT_OK || data == null)
            return;

        final Uri PATH = data.getData();
        binding.imageEditText.setText(UploadImage.getImageName(PATH, this));
        bitmap = UploadImage.getBitmapFromPath(getContentResolver(), PATH);
    }

    private void update() {
        if (bitmap != null) {

        }
    }

    private void upload(
            String name,
            String price,
            String quantity,
            String imageName
    ) {
        progressDialog.show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage
                .getReference("images")
                .child(imageName);
        UploadTask uploadTask = reference.putBytes(baos.toByteArray());

        uploadTask.addOnSuccessListener(taskSnapshot -> {

            if (taskSnapshot.getMetadata() == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskSnapshot.getMetadata().getReference() == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show();
                return;
            }

            taskSnapshot.getMetadata().getReference()
                    .getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() == null) {
                            Toast.makeText(this, "Gagal!", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        saveData(name, price, quantity, task.getResult().toString());
                    });

        }).addOnFailureListener(error -> {
            progressDialog.dismiss();
            Toast.makeText(
                    this,
                    "Gagal: " + error.getLocalizedMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void updateWithImage() {

    }
}