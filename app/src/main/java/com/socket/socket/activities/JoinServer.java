package com.socket.socket.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.InetAddresses;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.socket.socket.R;
import com.socket.socket.engine.Client;
import com.socket.socket.engine.Server;
import com.socket.socket.firebase.FirebaseClass;
import com.socket.socket.utility.Utility;

import static com.socket.socket.utility.Utility.getMd5;

public class JoinServer extends AppCompatActivity{
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Si imposta il layout da visualizzare;
        setContentView(R.layout.join_server);

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this);

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        setListeners();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setListeners(){
        Button joinViaIpBtn = this.findViewById(R.id.join_joinviaip);
        Button serversListBtn = this.findViewById(R.id.join_serverslist);
        Button goBackBtn = this.findViewById(R.id.join_goback);

        joinViaIpBtn.setOnClickListener(v -> joinServerDialog());
        serversListBtn.setOnClickListener(v -> serversListDialog());
        goBackBtn.setOnClickListener(v -> this.onBackPressed());
    }

    /**
     * Questo metodo crea una input dialog che permette di inserire le informazioni del server a cui il client dovrà connettersi;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void joinServerDialog(){
        // Si crea una nuova dialog;
        Dialog dialog = new Dialog(this);

        // Si imposta il layout della dialog;
        dialog.setContentView(R.layout.input_join_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.input_parent);
        TextInputEditText addressInput = dialog.findViewById(R.id.input_addressDialog);
        TextInputEditText passwordInput = dialog.findViewById(R.id.input_passwordDialog);
        Button confirmBtn = dialog.findViewById(R.id.input_okDialog);

        ImageView close = dialog.findViewById(R.id.input_closeDialog);

        // Si ridimensiona la dialog in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, parentView);

        // Si imposta un listener al bottone di conferma;
        confirmBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            String password = getMd5(passwordInput.getText().toString().trim());
            String addressFB = address.replace(".", "_");

            // Se l'indirizzo IP non è un indirizzo valido si visualizza un messaggio di errore;
            if(!InetAddresses.isNumericAddress(address)){
                Utility.oneLineDialog(this, getString(R.string.invalidaddress), null);
                return;
            }

            checkPasswordAndConnect(addressFB, password, () -> {
                // Starta una nuova activity passandole le informazioni inserite nella dialog;
                Intent i = new Intent(this, Client.class);
                i.putExtra("address", address);
                i.putExtra("pw", password);

                this.startActivity(i);
            }, () -> {
                Utility.oneLineDialog(this, this.getString(R.string.invalidiporpw), null);
            });
        });

        close.setOnClickListener(v -> {
            // Chiude la dialog;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkPasswordAndConnect(String serverAddress, String passwordEntered, Runnable onConfirm, Runnable onDeny){
        FirebaseClass.getDBRef().child(serverAddress).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Server.ServerInstance serverInstance = snapshot.getValue(Server.ServerInstance.class);
                if(serverInstance.getServerPW().equals(passwordEntered))
                    onConfirm.run();
                else
                    onDeny.run();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                onDeny.run();
            }
        });
    }

    private void serversListDialog(){}
}
