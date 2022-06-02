package com.example.liveeasy.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UploadImage {
    @SuppressLint("Range")
    public static String getImageName(Uri uri, Context ctx) {
        String res = "";

        if (!uri.getScheme().equals("content"))
            return res;

        Cursor cursor = ctx.getContentResolver().query(
                uri, null, null, null, null
        );

        try {
            if (cursor == null || !cursor.moveToFirst())
                return res;
            res = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            );
        } finally {
            cursor.close();
        }

        if (res != null)
            return res;

        res = uri.getPath();
        int cutt = res.lastIndexOf('/');
        if (cutt != -1)
            res = res.substring(cutt + 1);

        return res;
    }

    public static Bitmap getBitmapFromPath(ContentResolver content, Uri path) {
        try {
            return MediaStore.Images.Media.getBitmap(content, path);
        } catch (Exception e) {
            return null;
        }
    }

    public static void deleteImageFromURL(
            String url,
            Activity context,
            ProgressDialog progressDialog
    ) {
        StorageReference imageRef = FirebaseStorage
                .getInstance().getReferenceFromUrl(url);
        imageRef.delete().addOnSuccessListener(success -> {
            Toast.makeText(
                    context,
                    "Successfully deleted medicine!",
                    Toast.LENGTH_LONG
            ).show();
            progressDialog.dismiss();
        }).addOnFailureListener(error -> {
            Toast.makeText(
                    context,
                    "Failed to delete medicine: " + error.getLocalizedMessage(),
                    Toast.LENGTH_LONG
            ).show();
            progressDialog.dismiss();
        });
    }

}
