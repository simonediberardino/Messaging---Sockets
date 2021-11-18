package com.socket.socket.engine;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.socket.socket.R;
import com.socket.socket.utility.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends AppCompatActivity{
    // Indirizzo del socket da creare;
    private String SERVER_IP;
    // Porta del socket da creare (Default: 8080);
    private final int SERVER_PORT = 8080;

    private ArrayList<Instance> instances;
    private ServerSocket serverSocket;

    // Oggetti relativi all'interfaccia grafica;
    private TextView textViewIP, textViewPort;
    private TextView textViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonInvio;
    private ScrollView chatSV;

    /**
     * Metodo che viene eseguito non appena l'activity viene creata;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Si imposta il layout da visualizzare;
        this.setContentView(R.layout.main_server);

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this);

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        // Inizializzazione degli elementi dell'interfaccia grafica e dei listener;
        initializate();

        // Creazione di un nuovo thread per non interrompere il main thread durante la creazione del server socket;
        startSocketServer();
        new Thread(this::listenClients).start();
    }

    /**
     * Questo metodo assegna ad ogni attributo dell'oggetto la relativa view dell'interfaccia grafica (utilizzando il loro ID) e setta gli eventuali listener;
     * @return void;
     */
    private void initializate() {
        textViewIP = findViewById(R.id.server_info_address);
        textViewPort = findViewById(R.id.server_info_port);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);
        chatSV = findViewById(R.id.server_scrollView);

        SERVER_IP = getLocalIpAddress();

        instances = new ArrayList<>();

        // La box che permette di inviare i messaggi al client è disattivata fino a quando il client non si connette al server;
        editTextMessage.setEnabled(false);

        buttonInvio.setOnClickListener(v -> {
            // Assegnazione e pulizia della stringa contenuta nel box;
            String message = editTextMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Creazione di un nuovo thread per non interrompere il main thread durante l'invio del messaggio al client;
                new Thread(() -> {
                    try{
                        for(Instance i : instances)
                            sendMessage(i, message);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Questo metodo crea e starta il server socket e aggiorna le TextView con le informazioni relative al socket;
     * @return void;
     */
    private void startSocketServer(){
        try {
            // Creazione del socket;
            serverSocket = new ServerSocket(SERVER_PORT);

            System.out.printf("Socket creato con successo! Ascoltando sulla porta %d.\n", SERVER_PORT);

            runOnUiThread(() -> {
                textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
                textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenClients(){
        Socket socket;

        while(serverSocket != null){
            try {
                socket = serverSocket.accept();

                Instance clientInstance = new Instance(new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()));
                instances.add(clientInstance);

                runOnUiThread(() -> {
                    // Dialog di conferma;
                    Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null);
                    editTextMessage.setEnabled(true);
                });

                // Il server si mette in ascolto del client;
                new Thread(() -> messageListener(clientInstance)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * In questo metodo l'oggetto input si mette in ascolto del client e vengono stampati gli eventuali messaggi in entrata;
     * @return void;
     */
    private void messageListener(Instance instance){
        System.out.println("Server in ascolto del client.");

        // Si mette in ascolto del client fin tanto che l'oggetto input è definito;
        while (instance.getInput() != null) {
            try {
                // Si legge il messaggio in entrata;
                final String message = instance.getInput().readUTF();
                // Lo si stampa sono nel caso in cui non sia null;
                if (message != null) {
                    System.out.printf("Messaggio ricevuto: %s.\n", message);

                    for(Instance i : instances){
                        if(i != instance)
                            sendMessage(i, message);
                    }

                    runOnUiThread(() -> {
                        textViewMessages.append(String.format("%s: %s\n", getString(R.string.client), message));

                        // Scrolla la chat all'ultima riga ogni volta che si riceve un messaggio;
                        chatSV.fullScroll(View.FOCUS_DOWN);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Questo metodo invia un messaggio al client utilizzando l'oggetto che gestisce i messaggi in uscita;
     * @param message da inviare al server;
     * @throws IOException se si riscontra un errore durante l'invio di un messaggio al client;
     */
    private void sendMessage(Instance instance, String message) throws IOException{
        instance.getOutput().writeUTF(message);
        instance.getOutput().flush();

        System.out.printf("%s inviato con successo.\n", message);

        runOnUiThread(() -> {
            textViewMessages.append(String.format("%s: %s\n", getString(R.string.server), message));

            // Scrolla la chat all'ultima riga ogni volta che si invia un messaggio;
            chatSV.fullScroll(View.FOCUS_DOWN);
            editTextMessage.setText(new String());
        });
    }

    /**
     * Questo metodo restituisce l'indirizzo IP locale in formato stringa;
     * @return indirizzo IP locale;
     */
    private String getLocalIpAddress(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    /**
     * Override del metodo implementato in AppCompactActivity;
     * Chiude il socket e gli oggetti di input e output ogni volta che l'activity viene terminata o messa in pausa;
     * @return void;
     */
    @Override
    protected void onStop(){
        super.onStop();

        try{
            /* Si chiudono i socket e gli oggetti di input e output (Dopo aver effettuato un check per assicurarsi che non
            siano null per evitare la NullPointerException; */
            if(serverSocket != null)
                serverSocket.close();

            for(Instance i : instances){
                if(i.getOutput() != null)
                    i.getOutput().close();

                if(i.getInput() != null)
                    i.getInput().close();
            }

            System.out.println("Socket chiuso con successo.");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Override del metodo che viene invocato ogni volta che si preme il bottone "indietro";
     * Visualizza una dialog di conferma prima di chiudere l'applicazione;
     */
    @Override
    public void onBackPressed() {
        Utility.oneLineDialog(this, this.getString(R.string.closesocket), super::onBackPressed);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);
    }

    private static class Instance{
        private final DataOutputStream output;
        private final DataInputStream input;

        public Instance(DataOutputStream output, DataInputStream input){
            this.output = output;
            this.input = input;
        }

        public DataOutputStream getOutput(){
            return output;
        }

        public DataInputStream getInput(){
            return input;
        }
    }
}