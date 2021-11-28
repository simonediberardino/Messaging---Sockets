package com.socket.socket.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.socket.socket.engine.Server.ServerInstance
import com.socket.socket.entity.Utente

object FirebaseClass {
    var DB_ID: String? = "https://socket-chat-aab65-default-rtdb.europe-west1.firebasedatabase.app/"
    fun isFirebaseStringValid(string: String?): Boolean {
        var string = string
        string = string?.trim { it <= ' ' }
        return !(string == "null" || string?.isEmpty() == true || string?.contains(".") == true || string?.contains("#") == true || string?.contains("$") == true || string?.contains("[") == true || string?.contains("]") == true)
    }

    fun getDBRef(): DatabaseReference? {
        return FirebaseDatabase.getInstance(DB_ID!!).reference
    }

    fun getSpecificUser(path: String?): DatabaseReference? {
        return FirebaseDatabase.getInstance(DB_ID!!).getReference(path!!)
    }

    fun addUserToFirebase(email: String?, utente: Utente?) {
        getDBRef()?.child(email!!)?.setValue(utente)
    }

    fun addServerToFirebase(serverIp: String?, server: ServerInstance?) {
        getDBRef()?.child(serverIp!!)?.setValue(server)
    }

    fun <T> editFieldFirebase(username: String?, fieldToUpdate: String?, value: T?) {
        val update = getSpecificUser(username)
        update?.child(fieldToUpdate!!)?.setValue(value)
    }

    fun <T> deleteFieldFirebase(specific: String?, field: String?) {
        if (specific != null) getSpecificUser(specific)?.child(field!!)
            ?.removeValue() else getDBRef()?.child(field!!)?.removeValue()
    }
}