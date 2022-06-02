package com.example.liveeasy.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.liveeasy.activities.MainActivity;
import com.example.liveeasy.activities.UpdateMedActivity;
import com.example.liveeasy.dao.MedDAO;
import com.example.liveeasy.databinding.ViewholderMedBinding;
import com.example.liveeasy.helpers.PriceFormatter;
import com.example.liveeasy.helpers.UploadImage;
import com.example.liveeasy.models.Medicine;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MedAdapter extends RecyclerView.Adapter<MedAdapter.ListViewHolder> {
    private final Activity context;
    private ArrayList<Medicine> listMed = new ArrayList<>();
    private final MedDAO medDAO;

    public MedAdapter(Activity context, MedDAO medDAO) {
        this.context = context;
        this.medDAO = medDAO;
    }

    public void setListMed(ArrayList<Medicine> listMed) {
        this.listMed = listMed;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderMedBinding binding;
        private final Activity context;
        private final MedDAO medDAO;
        private ProgressDialog progressDialog;
        FirebaseStorage storage = FirebaseStorage.getInstance();

        public ListViewHolder(
                @NonNull ViewholderMedBinding binding,
                Activity context,
                MedDAO medDAO
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.medDAO = medDAO;
            initProgressDialog();
        }

        public void bindView(Medicine med) {
            binding.nameTextView.setText(med.getName());
            binding.priceTextView.setText(PriceFormatter.rupiahFormat(med.getPrice()));
            binding.qtyTextView.setText("Qty: " + med.getQuantity());
            binding.deleteButton.setOnClickListener(view -> deleteMed(med));
            binding.updateButton.setOnClickListener(view -> updateMed(med));
            Glide.with(context).load(med.getImage()).into(binding.medImageView);
        }

        private void initProgressDialog() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Just a few second...");
            progressDialog.setCancelable(false);
        }

        private void deleteMed(Medicine med) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Delete");
            alert.setMessage("Are you sure want to delete this medicine?");
            alert.setPositiveButton("Yes", (dialog, id) -> {
                progressDialog.show();
                medDAO.delete(med.getKey()).addOnSuccessListener(res -> {
                    UploadImage.deleteImageFromURL(med.getImage(), context, progressDialog);
                }).addOnFailureListener(error -> {
                    Toast.makeText(
                            context,
                            "Failed to delete medicine: " + error.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    progressDialog.dismiss();
                });
            });
            alert.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            alert.show();
        }

        private void updateMed(Medicine med) {
            Intent intent = new Intent(context, UpdateMedActivity.class);
            intent.putExtra("KEY", med.getKey());
            context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewholderMedBinding binding = ViewholderMedBinding.inflate(
                layoutInflater,
                parent,
                false
        );

        return new ListViewHolder(binding, context, medDAO);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ListViewHolder holder,
            int position
    ) {
        holder.bindView(listMed.get(position));
    }

    @Override
    public int getItemCount() {
        return listMed.size();
    }
}
