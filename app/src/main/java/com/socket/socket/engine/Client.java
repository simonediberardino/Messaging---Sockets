package com.socket.socket.engine;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends AppCompatActivity{
    private String SERVER_IP;
    private int SERVER_PORT;

    private PrintWriter output;
    private BufferedReader input;

    private Socket socket;
    private EditText editTextIP, editTextPort;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private Button buttonInvio;
    private Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main_client);

        initializate();
    }

    private void initializate(){
        editTextIP = findViewById(R.id.etIP);
        editTextPort = findViewById(R.id.etPort);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);
        buttonConnect = findViewById(R.id.btnConnect);

        buttonConnect.setOnClickListener(v -> {
            textViewMessages.setText(new String());
            SERVER_IP = editTextIP.getText().toString().trim();
            SERVER_PORT = Integer.parseInt(editTextPort.getText().toString().trim());

            new Thread(this::connectServer).start();
        });

        buttonInvio.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                new Thread(() -> sendMessage(message)).start();
            }
        });
    }

    private void connectServer(){
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            output = new PrintWriter(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            runOnUiThread(() -> textViewMessages.setText(String.format("%s\n", getString(R.string.connected))));

            new Thread(this::messageListener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageListener(){
        while (true) {
            try {
                final String message = input.readLine();
                if (message != null) {
                    runOnUiThread(() -> textViewMessages.append(String.format("Server: %s\n", message)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message){
        output.write(message);
        output.flush();
        output.close();

        runOnUiThread(() -> {
            textViewMessages.append(String.format("Client: %s\n", message));
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
