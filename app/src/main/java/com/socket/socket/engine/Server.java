package com.socket.socket.engine;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Server extends AppCompatActivity{
    private String SERVER_IP;
    private final int SERVER_PORT = 8080;

    private DataOutputStream output;
    private DataInputStream input;

    private ServerSocket serverSocket;
    private TextView textViewIP, textViewPort;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonInvio;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_server);

        Utility.enableTopBar(this);
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        initializate();
    }

    private void initializate(){
        textViewIP = findViewById(R.id.server_info_address);
        textViewPort = findViewById(R.id.server_info_port);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        editTextMessage.setEnabled(false);

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

        new Thread(this::startSocketServer).start();
    }
    
    private void startSocketServer(){
        final Socket socket;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);

            runOnUiThread(() -> {
                textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
                textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));
            });

            try {
                socket = serverSocket.accept();
                output = new DataOutputStream(socket.getOutputStream());
                input = new DataInputStream(socket.getInputStream());

                runOnUiThread(() -> {
                    Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null);
                    editTextMessage.setEnabled(true);
                });

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
                final String message = input.readUTF();
                if (message != null) {
                    runOnUiThread(() -> textViewMessages.append(String.format("%s: %s\n", getString(R.string.client), message)));
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
            textViewMessages.append(String.format("%s: %s\n", getString(R.string.server), message));
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