package com.example.liveeasy.helpers;

public class PriceFormatter {
    public static String rupiahFormat(String price){
        String s = String.format("%,d", Integer.parseInt(price));
        return "Rp " + s.replace(",", ".");
    }
}
