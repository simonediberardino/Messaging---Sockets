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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Utility.enableTopBar(this);
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        setListeners();
    }

    private void setListeners(){
        Button startServer = findViewById(R.id.main_startserver);
        Button joinServer = findViewById(R.id.main_joinserver);
        Button closeApp = findViewById(R.id.main_closeapp);

        startServer.setOnClickListener(v -> {
            Utility.goTo(this, Server.class);
        });

        joinServer.setOnClickListener(v -> {
            joinServerDialog();
        });

        closeApp.setOnClickListener(v -> {
            this.onBackPressed();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void joinServerDialog(){
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.input_join_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.input_parent);
        TextInputEditText addressInput = dialog.findViewById(R.id.input_addressDialog);
        TextInputEditText portInput = dialog.findViewById(R.id.input_portDialog);
        Button confirmBtn = dialog.findViewById(R.id.input_okDialog);

        ImageView close = dialog.findViewById(R.id.input_closeDialog);

        Utility.ridimensionamento(this, parentView);

        confirmBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString();

            int port;
            try{
                port = Integer.parseInt(portInput.getText().toString());
            }catch(Exception e){
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            if(!InetAddresses.isNumericAddress(address)){
                Utility.oneLineDialog(this, getString(R.string.invalidaddress), null);
                return;
            }

            if(port < 1024 || port > 65535){
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            Intent i = new Intent(this, Client.class);
            i.putExtra("address", address);
            i.putExtra("port", port);

            this.startActivity(i);
        });

        close.setOnClickListener(v -> {
            this.onBackPressed();
        });

        dialog.show();
    }

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
