package com.socket.socket.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Contacts;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility{
    public static String getMd5(String input){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);

            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void navigateTo(AppCompatActivity c, Class cl){
        Intent i = new Intent(c,cl);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
    }

    public static void oneLineDialog(Context c, String title, Runnable callback){
        new com.socket.socket.UI.CDialog((Activity) c, title, callback).show();
    }

    public static void oneLineDialog(Context c, String title, String option1, String option2, Runnable firstCallback, Runnable secondCallback, Runnable dismissCallback){
        new com.socket.socket.UI.CDialog((Activity) c, title, option1, option2, firstCallback, secondCallback, dismissCallback).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void enableTopBar(AppCompatActivity c){
        int resId = c.getResources().getIdentifier("topbar", "drawable", c.getPackageName());
        c.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        c.getSupportActionBar().setBackgroundDrawable(c.getResources().getDrawable(resId));
        c.getSupportActionBar().setCustomView(R.layout.actionbar);

        String idS = "lefticon";
        int id = c.getResources().getIdentifier(idS, "id", c.getPackageName());
        c.findViewById(id).setOnClickListener(v -> c.onBackPressed());
    }

    // Ridimensiona i componenti in base alla dimensione dello schermo, NOTA: da utilizzare ogni qual volta si cambia la content view;
    public static void ridimensionamento(AppCompatActivity activity, ViewGroup v){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        final double baseHeight = 1920;
        double height = displayMetrics.heightPixels;

        for (int i = 0; i < v.getChildCount(); i++) {
            View vAtI = v.getChildAt(i);

            int curHeight = vAtI.getLayoutParams().height;
            int curWidth = vAtI.getLayoutParams().width;
            double rapporto = height/baseHeight;

            if(curHeight > ViewGroup.LayoutParams.MATCH_PARENT)
                vAtI.getLayoutParams().height = (int) (curHeight * rapporto);

            if(curWidth > ViewGroup.LayoutParams.MATCH_PARENT)
                vAtI.getLayoutParams().width = (int) (curWidth * rapporto);

            if(vAtI instanceof TextView){
                int curSize = (int) ((TextView) vAtI).getTextSize();
                int newSize = (int) (curSize * rapporto);

                ((TextView) vAtI).setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }

            vAtI.requestLayout();

            if(vAtI instanceof ViewGroup){
                ridimensionamento(activity, (ViewGroup) vAtI);
            }
        }
    }
}
