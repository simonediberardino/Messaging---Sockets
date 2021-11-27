package com.socket.socket.utility

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.socket.socket.data.SharedPrefs
import com.socket.socket.firebase.FirebaseClass

object LoginUtility {
    fun getUsername(): String? {
        return SharedPrefs.getUsername()
    }

    fun isUser(d: DataSnapshot?): Boolean {
        return d.hasChild("username")
    }

    fun isLoggedIn(): Boolean {
        return SharedPrefs.getUsername() != "null"
    }

    fun updateEmail() {
        FirebaseClass.getDBRef().child(SharedPrefs.getEmail())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot?) {
                    for (d in snapshot.getChildren()) {
                        if (d.key == "email") {
                            SharedPrefs.setEmail(d.value.toString())
                            break
                        }
                    }
                }

                override fun onCancelled(@NotNull error: DatabaseError) {}
            })
    }
}