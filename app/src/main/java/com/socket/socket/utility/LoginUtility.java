package com.socket.socket.utility;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.socket.socket.data.SharedPrefs;
import com.socket.socket.firebase.FirebaseClass;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginUtility{
    public static String getUsername(){
        return SharedPrefs.getUsername();
    }

    public static boolean isUser(DataSnapshot d){
        return d.hasChild("username");
    }

    public static boolean isLoggedIn(){
        return !SharedPrefs.getUsername().equals("null");
    }

    public static void updateEmail() {
        FirebaseClass.getDBRef().child(SharedPrefs.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot d : snapshot.getChildren()) {
                    if(d.getKey().equals("email")){
                        SharedPrefs.setEmail(String.valueOf(d.getValue()));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
