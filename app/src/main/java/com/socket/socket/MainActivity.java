package com.socket.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListeners();
    }

    private void setListeners(){
        Button startServer = findViewById(R.id.main_startserver);
        Button joinServer = findViewById(R.id.main_joinserver);

        startServer.setOnClickListener(v -> {
            Utility.goTo(this, Server.class);
        });

        joinServer.setOnClickListener(v -> {
            Utility.goTo(this, Client.class);
        });
    }
}
