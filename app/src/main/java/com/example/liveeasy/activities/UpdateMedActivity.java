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

public class UpdateMedActivity extends AppCompatActivity {

    private LayoutFormBinding binding;
    private final MedDAO medDao = new MedDAO();
    private ProgressDialog progressDialog;
    private Bitmap bitmap;
    private Medicine med;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initProgressDialog();

        key = getIntent().getStringExtra("KEY");
        getMed();

        binding.uploadButton.setOnClickListener(view -> chooseImage());
        binding.submitButton.setOnClickListener(view -> update());
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Just a few second...");
        progressDialog.setCancelable(false);
    }

    private void getMed() {
        medDao.getByKey(key).addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                return;
            med = task.getResult().getValue(Medicine.class);
            binding.medEditText.setText(med.getName());
            binding.qtyEditText.setText(med.getQuantity());
            binding.priceEditText.setText(med.getPrice());
        });
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
        String name = binding.medEditText.getText().toString();
        String price = binding.priceEditText.getText().toString();
        String qty = binding.qtyEditText.getText().toString();
        String image = Timestamp.addTimestampToImage(
                binding.imageEditText.getText().toString()
        );

        String validationErrMsg;
        validationErrMsg = validateForm(name, price, qty, image, bitmap != null);

        if (!validationErrMsg.isEmpty()) {
            Toast.makeText(
                    this,
                    validationErrMsg,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (bitmap != null) {
            deleteImage(name, price, qty, med.getImage(), image);
        } else {
            updateData(name, price, qty, med.getImage());
        }
    }

    private String validateForm(
            String name,
            String price,
            String qty,
            String image,
            boolean isWithImage
    ) {
        if (name.isEmpty() || price.isEmpty() || qty.isEmpty())
            return "Please fill out all required fields";
        if (isWithImage && image.isEmpty())
            return "Please fill the image name";
        return "";

    }

    private void deleteImage(
            String name,
            String price,
            String qty,
            String deletedImage,
            String updatedImage
    ) {
        progressDialog.show();
        StorageReference imageRef = FirebaseStorage
                .getInstance().getReferenceFromUrl(deletedImage);
        imageRef.delete().addOnSuccessListener(success ->
                upload(name, price, qty, updatedImage)
        ).addOnFailureListener(error -> {
            Toast.makeText(
                    this,
                    "Failed to update medicine: " + error.getLocalizedMessage(),
                    Toast.LENGTH_LONG
            ).show();
            progressDialog.dismiss();
        });
    }

    private void upload(
            String name,
            String price,
            String quantity,
            String imageName
    ) {
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
                        updateData(name, price, quantity, task.getResult().toString());
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

    private void updateData(
            String name,
            String price,
            String quantity,
            String imageName
    ) {
        if (!progressDialog.isShowing())
            progressDialog.show();

        med.setName(name);
        med.setPrice(price);
        med.setQuantity(quantity);
        med.setImage(imageName);
        medDao.update(key, med).addOnSuccessListener(res -> {
            Toast.makeText(
                    this,
                    "Successfully updated medicine",
                    Toast.LENGTH_LONG
            ).show();
            startActivity(new Intent(this, MainActivity.class));
        }).addOnFailureListener(error -> {
            progressDialog.dismiss();
            Toast.makeText(
                    this,
                    "Failed to update medicine: " + error.getLocalizedMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}