package com.socket.socket.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.socket.socket.entity.Utente;

public class FirebaseClass {
    public static String DB_ID = "https://socket-chat-aab65-default-rtdb.europe-west1.firebasedatabase.app/";

    public static boolean isFirebaseStringValid(String string)
    {
        string = string.trim();

        if(string.equals("null") || string.isEmpty() || string.contains(".")  || string.contains("#") || string.contains("$") || string.contains("[") || string.contains("]"))
            return false;
        else
            return true;
    }

    public static DatabaseReference getDBRef()
    {
        return FirebaseDatabase.getInstance(DB_ID).getReference();
    }

    public static DatabaseReference getSpecificUser(String path)
    {
        return FirebaseDatabase.getInstance(DB_ID).getReference(path);
    }

    public static void addToFirebase(String username, Utente utente)
    {
        getDBRef().child(username).setValue(utente);
    }


    public static <T> void editFieldFirebase(String username, String fieldToUpdate, T value)
    {
        DatabaseReference update = getSpecificUser(username);
        update.child(fieldToUpdate).setValue(value);
    }

    public static <T> void deleteFieldFirebase(String specific, String field)
    {
        if(specific != null)
            getSpecificUser(specific).child(field).removeValue();
        else
            getDBRef().child(field).removeValue();
    }
}
