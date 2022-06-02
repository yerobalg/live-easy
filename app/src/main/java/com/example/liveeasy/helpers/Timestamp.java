package com.example.liveeasy.helpers;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Timestamp {
    @SuppressLint("SimpleDateFormat")
    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")
                .format(Calendar.getInstance().getTime());
    }

    public static String addTimestampToImage(String image) {
        String[] split = image.split("\\.");
        split[split.length - 2] += "_" + getCurrentTimestamp();
        return TextUtils.join(".", split);
    }
}
