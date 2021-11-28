package com.socket.socket.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.socket.socket.R
import com.socket.socket.engine.Client
import com.socket.socket.engine.Server.ServerInstance
import com.socket.socket.firebase.FirebaseClass
import com.socket.socket.utility.Utility

class JoinServer : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Si imposta il layout da visualizzare;
        setContentView(R.layout.join_server)

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this)

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent))
        setListeners()
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun setListeners() {
        val joinViaIpBtn = findViewById<Button?>(R.id.join_joinviaip)
        val serversListBtn = findViewById<Button?>(R.id.join_serverslist)
        val goBackBtn = findViewById<Button?>(R.id.join_goback)
        joinViaIpBtn.setOnClickListener { joinServerDialog() }
        serversListBtn.setOnClickListener { serversListDialog() }
        goBackBtn.setOnClickListener { onBackPressed() }
    }

    /**
     * Questo metodo crea una input dialog che permette di inserire le informazioni del server a cui il client dovrà connettersi;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun joinServerDialog() {
        // Si crea una nuova dialog;
        val dialog = Dialog(this)

        // Si imposta il layout della dialog;
        dialog.setContentView(R.layout.input_join_server)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val parentView = dialog.findViewById<ViewGroup?>(R.id.input_parent)
        val addressInput: TextInputEditText = dialog.findViewById(R.id.input_addressDialog)
        val passwordInput: TextInputEditText = dialog.findViewById(R.id.input_passwordDialog)
        val confirmBtn = dialog.findViewById<Button?>(R.id.input_okDialog)
        val close = dialog.findViewById<ImageView?>(R.id.input_closeDialog)

        // Si ridimensiona la dialog in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, parentView)

        // Si imposta un listener al bottone di conferma;
        confirmBtn.setOnClickListener {
            val address = addressInput.text.toString().trim { it <= ' ' }
            val password = Utility.getMd5(passwordInput.text.toString().trim { it <= ' ' })
            val addressFB = address.replace(".", "_")

            // Se l'indirizzo IP non è un indirizzo valido si visualizza un messaggio di errore;
            if (!InetAddresses.isNumericAddress(address)) {
                Utility.oneLineDialog(this, getString(R.string.invalidaddress), null)
                return@setOnClickListener
            }
            checkPasswordAndConnect(addressFB, password, {

                // Starta una nuova activity passandole le informazioni inserite nella dialog;
                val i = Intent(this, Client::class.java)
                i.putExtra("address", address)
                i.putExtra("pw", password)
                this.startActivity(i)
            }) { Utility.oneLineDialog(this, this.getString(R.string.invalidiporpw), null) }
        }
        close.setOnClickListener {
            // Chiude la dialog;
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkPasswordAndConnect(
        serverAddress: String?,
        passwordEntered: String?,
        onConfirm: Runnable?,
        onDeny: Runnable?
    ) {
        if (serverAddress != null) {
            FirebaseClass.getDBRef()?.child(serverAddress)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val serverInstance = snapshot.getValue(
                            ServerInstance::class.java
                        )
                        if (serverInstance?.getServerPW() == passwordEntered) onConfirm?.run() else onDeny?.run()
                    }

                    override fun onCancelled(@NotNull error: DatabaseError) {
                        onDeny?.run()
                    }
                })
        }
    }

    private fun serversListDialog() {}
}