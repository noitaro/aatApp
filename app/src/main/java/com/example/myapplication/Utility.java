package com.example.myapplication;

public class Utility {

    public static String trim(String str, char c) {
        if(str.charAt(0) == c && str.charAt(str.length()-1) == c) {
            return str.substring(1, str.length()-1);
        }else {
            return str;
        }
    }
}
