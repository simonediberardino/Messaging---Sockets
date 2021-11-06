package com.socket.socket.utility;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPref {
    private static Context context;

    private static final String SHARED_PREFS = "sharedPrefs";

    private static final String USERNAME_ID = "USERNAME";
    private static final String PASSWORD_ID = "PASSWORD";
    private static final String EMAIL_ID = "EMAIL";

    private static final String USERNAME_DEFAULT = "null";
    private static final String PASSWORD_DEFAULT = "null";
    private static final String EMAIL_DEFAULT = "null";

    public static void setUsername(String username){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(USERNAME_ID, username);
        editor.apply();
    }

    public static void setEmail(String email)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(EMAIL_ID, email);
        editor.apply();
    }

    public static String getEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(EMAIL_ID, EMAIL_DEFAULT);
    }

    public static String getUsername(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME_ID, USERNAME_DEFAULT);
    }

    public static void setPassword(String password){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PASSWORD_ID, password);
        editor.apply();
    }

    public static String getPassword(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(PASSWORD_ID, PASSWORD_DEFAULT);
    }

    public static void setContext(Context context){
        SharedPref.context = context;
    }
}
