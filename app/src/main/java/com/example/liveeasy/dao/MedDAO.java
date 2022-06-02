package com.example.liveeasy.dao;


import android.util.Log;

import com.example.liveeasy.models.Medicine;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MedDAO {
    private DatabaseReference databaseReference;

    public MedDAO(){
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://liveeasy-355fb-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = db.getReference(Medicine.class.getSimpleName());
    }

    public Task<Void> insert(Medicine med) {
        return databaseReference.push().setValue(med);
    }

    public Task<Void> update (String key, Medicine med) {
        return databaseReference.child(key).setValue(med);
    }

    public Task<Void> delete (String key) {
        return databaseReference.child(key).removeValue();
    }

    public Query get() {
        return databaseReference.orderByKey();
    }

    public Task<DataSnapshot> getByKey(String key) {
        return databaseReference.child(key).get();
    }
}

