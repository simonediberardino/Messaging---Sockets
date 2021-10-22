package com.socket.socket.engine;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Server extends AppCompatActivity{
    private String SERVER_IP;
    private final int SERVER_PORT = 8080;

    private PrintWriter output;
    private BufferedReader input;

    private ServerSocket serverSocket;
    private TextView textViewIP, textViewPort;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private Button buttonInvio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_server);
        
        initializate();
    }

    private void initializate(){
        textViewIP = findViewById(R.id.server_out_ip);
        textViewPort = findViewById(R.id.server_out_port);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        buttonInvio.setOnClickListener(v -> {
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                new Thread(() -> sendMessage(message)).start();
            }
        });

        new Thread(this::startSocketServer).start();
    }
    
    private void startSocketServer(){
        final Socket socket;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);

            runOnUiThread(() -> {
                textViewMessages.setText(getString(R.string.notconnected));
                textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
                textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));
            });

            try {
                socket = serverSocket.accept();
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                runOnUiThread(() -> textViewMessages.setText(String.format("%s\n", getString(R.string.connected))));

                new Thread(this::messageListener).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void messageListener(){
        while (true) {
            try {
                final String message = input.readLine();
                if (message != null) {
                    runOnUiThread(() -> textViewMessages.append(String.format("Client: %s\n", message)));
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
            textViewMessages.append(String.format("Server: %s\n", message));
            editTextMessage.setText(new String());
        });
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            if(serverSocket != null)
                serverSocket.close();

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