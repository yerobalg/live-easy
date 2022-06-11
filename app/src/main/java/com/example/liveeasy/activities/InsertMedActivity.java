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
import com.example.liveeasy.helpers.Timestamp;
import com.example.liveeasy.helpers.UploadImage;
import com.example.liveeasy.models.Medicine;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class InsertMedActivity extends AppCompatActivity {

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
        binding.submitButton.setOnClickListener(view -> insert());
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Just a few second...");
        progressDialog.setCancelable(false);
    }

    private String validateForm(
            String name,
            String price,
            String quantity,
            String imageName
    ) {
        if (name.isEmpty() || price.isEmpty() || quantity.isEmpty() || imageName.isEmpty())
            return "Please fill out all required fields";
        if (bitmap == null)
            return "Please upload an image";
        if (!imageName.contains(".jpeg") && !imageName.contains(".jpg") && !imageName.contains(".png"))
            return "image name must be in jpeg, jpg, or png format";
        return "";
    }

    private void insert() {
        String name = binding.medEditText.getText().toString();
        String price = binding.priceEditText.getText().toString();
        String quantity = binding.qtyEditText.getText().toString();
        String imageName = binding.imageEditText.getText().toString();

        String validationErrMsg = validateForm(name, price, quantity, imageName);
        if (!validationErrMsg.isEmpty()) {
            Toast.makeText(
                    this,
                    validationErrMsg,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        imageName = Timestamp.addTimestampToImage(
                binding.imageEditText.getText().toString()
        );
        upload(name, price, quantity, imageName);
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
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskSnapshot.getMetadata().getReference() == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                return;
            }

            taskSnapshot.getMetadata().getReference()
                    .getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() == null) {
                            Toast.makeText(this, "Failed!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        saveData(name, price, quantity, task.getResult().toString());
                    });

        }).addOnFailureListener(error -> {
            progressDialog.dismiss();
            Toast.makeText(
                    this,
                    "Failed: " + error.getLocalizedMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void saveData(
            String name,
            String price,
            String quantity,
            String imageName
    ) {
        medDao
                .insert(new Medicine(name, quantity, price, imageName))
                .addOnSuccessListener(success -> {
                    Toast.makeText(
                            this,
                            "Successfully added new medicine!",
                            Toast.LENGTH_LONG
                    ).show();
                    startActivity(
                            new Intent(this, MainActivity.class)
                    );
                }).addOnFailureListener(error -> {
            progressDialog.dismiss();
            Toast.makeText(
                    this,
                    "Failed to add medicine: " + error.getLocalizedMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}