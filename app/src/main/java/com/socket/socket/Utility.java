package com.socket.socket;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class Utility{
    public static void goTo(AppCompatActivity c, Class cl){
        Intent i = new Intent(c,cl);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
    }
}
