package com.socket.socket.engine;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;
import com.socket.socket.utility.Utility;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends AppCompatActivity{
    private String SERVER_IP;
    private int SERVER_PORT;

    private DataOutputStream output;
    private DataInputStream input;

    private Socket socket;
    private TextView textViewIP, textViewPort;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonInvio;
    private ScrollView chatSV;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.main_server);

        Utility.enableTopBar(this);
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        Bundle extras = getIntent().getExtras();

        SERVER_IP = extras.getString("address");
        SERVER_PORT = extras.getInt("port");

        initializate();
        new Thread(this::connectServer).start();

        final int timeout = 2000;
        new Handler().postDelayed(() -> {
            if(socket == null){
                Utility.oneLineDialog(this,
                        getString(R.string.connectionerror),
                        getString(R.string.ok),
                        getString(R.string.cancel),

                        this::onBackPressed,
                        this::onBackPressed,
                        this::onBackPressed);
            }
        }, timeout);
    }

    private void initializate(){
        textViewIP = findViewById(R.id.server_info_address);
        textViewPort = findViewById(R.id.server_info_port);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);
        chatSV = findViewById(R.id.server_scrollView);

        textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
        textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));

        buttonInvio.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                new Thread(() -> {
                    try{
                        sendMessage(message);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private void connectServer(){
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());

            runOnUiThread(() -> {
                Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null);
                textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
                textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));
            });

            new Thread(this::messageListener).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageListener(){
        while (true) {
            try {
                final String message = input.readUTF();
                if (message != null) {
                    runOnUiThread(() -> textViewMessages.append(String.format("%s: %s\n", getString(R.string.server), message)));
                    chatSV.fullScroll(View.FOCUS_DOWN);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) throws IOException{
        output.writeUTF(message);
        output.flush();

        runOnUiThread(() -> {
            textViewMessages.append(String.format("%s: %s\n", getString(R.string.client), message));
            chatSV.fullScroll(View.FOCUS_DOWN);
            editTextMessage.setText(new String());
        });
    }

    @Override
    protected void onStop(){
        super.onStop();

        try{
            if(socket != null)
                socket.close();

            if(output != null)
                output.close();

            if(input != null)
                input.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
