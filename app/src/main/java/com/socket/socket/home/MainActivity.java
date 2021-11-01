package com.socket.socket.home;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.socket.socket.engine.Client;
import com.socket.socket.R;
import com.socket.socket.engine.Server;
import com.socket.socket.utility.Utility;

public class MainActivity extends AppCompatActivity{
    /**
     * Metodo che viene eseguito non appena l'activity principale viene creata;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Si imposta il layout da visualizzare;
        setContentView(R.layout.activity_main);

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this);

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        setListeners();
    }

    /**
     * Questo metodo imposta i listener per ogni bottone presente nell'interfaccia grafica;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setListeners(){
        Button startServer = findViewById(R.id.main_startserver);
        Button joinServer = findViewById(R.id.main_joinserver);
        Button closeApp = findViewById(R.id.main_closeapp);

        startServer.setOnClickListener(v -> {
            Utility.navigateTo(this, Server.class);
        });

        joinServer.setOnClickListener(v -> {
            joinServerDialog();
        });

        closeApp.setOnClickListener(v -> {
            this.onBackPressed();
        });
    }

    /**
     * Questo metodo crea una input dialog che permette di inserire le informazioni del server a cui il client dovrà connettersi;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void joinServerDialog(){
        // Si crea una nuova dialog;
        Dialog dialog = new Dialog(this);

        // Si imposta il layout della dialog;
        dialog.setContentView(R.layout.input_join_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.input_parent);
        TextInputEditText addressInput = dialog.findViewById(R.id.input_addressDialog);
        TextInputEditText portInput = dialog.findViewById(R.id.input_portDialog);
        Button confirmBtn = dialog.findViewById(R.id.input_okDialog);

        ImageView close = dialog.findViewById(R.id.input_closeDialog);

        // Si ridimensiona la dialog in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, parentView);

        // Si imposta un listener al bottone di conferma;
        confirmBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString();

            int port;
            try{
                port = Integer.parseInt(portInput.getText().toString());
            }catch(Exception e){
                // Se la porta non è un numero intero si visualizza un messaggio di errore;
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            // Se l'indirizzo IP non è un indirizzo valido si visualizza un messaggio di errore;
            if(!InetAddresses.isNumericAddress(address)){
                Utility.oneLineDialog(this, getString(R.string.invalidaddress), null);
                return;
            }

            // Se la porta non appartiene al range di porte disponibili visualizza un messaggio di errore;
            final int PORT_MIN = 1024, PORT_MAX = 65535;
            if(port < PORT_MIN || port > PORT_MAX){
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            // Starta una nuova activity passandole le informazioni inserite nella dialog;
            Intent i = new Intent(this, Client.class);
            i.putExtra("address", address);
            i.putExtra("port", port);

            this.startActivity(i);
        });

        close.setOnClickListener(v -> {
            // Chiude la dialog;
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Override del metodo che viene invocato ogni volta che si preme il bottone "indietro";
     * Visualizza una dialog di conferma prima di chiudere l'applicazione;
     */
    @Override
    public void onBackPressed() {
        Utility.oneLineDialog(this, this.getString(R.string.confirmleave), this::finishAffinity);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);
    }
}
