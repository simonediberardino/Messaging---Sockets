package com.socket.socket.engine;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.socket.socket.R;
import com.socket.socket.activities.MainActivity;
import com.socket.socket.data.SharedPrefs;
import com.socket.socket.entity.Message;
import com.socket.socket.firebase.FirebaseClass;
import com.socket.socket.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.socket.socket.utility.LoginUtility.getUsername;

//@TODO: Aggiungere lista server;

public class Client extends AppCompatActivity{
    // Indirizzo IP del server a cui connettersi;
    private String SERVER_IP, SERVER_IP_FB;

    // Porta del server a cui connettersi;
    private int SERVER_PORT = Server.SERVER_PORT;

    // Oggetto di tipo DataOutputStream che gestirà i messaggi in uscita;
    private DataOutputStream output;

    // Oggetto di tipo DataInputStream che gestirà i messaggi in entrata;
    private DataInputStream input;

    private Socket socket;

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

        Bundle extras = getIntent().getExtras();

        // Si prelevano le informazioni inserite nella dialog nella activity precedente;
        String passwordEntered = extras.getString("pw");
        SERVER_IP = extras.getString("address");
        SERVER_IP_FB = SERVER_IP.replace(".", "_");

        // Inizializzazione degli elementi dell'interfaccia grafica e dei listener;
        initializate();

        handleTimeout();

        // Creazione di un nuovo thread per non interrompere il main thread durante la connessione al server;
        new Thread(this::connectToServer).start();
    }

    /**
    * Questo metodo assegna ad ogni attributo dell'oggetto la relativa view dell'interfaccia grafica (utilizzando il loro ID) e setta gli eventuali listener;
    * @return void;
    */
    private void initializate(){
        textViewIP = findViewById(R.id.server_info_address);
        textViewPort = findViewById(R.id.server_info_port);
        textViewMessages = findViewById(R.id.server_out_message);
        editTextMessage = findViewById(R.id.server_in_message);
        buttonInvio = findViewById(R.id.server_btn_send);
        chatSV = findViewById(R.id.server_scrollView);

        // Si scrive l'indirizzo IP del server nella TextView;
        textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));

        // Si scrive la porta del server nella TextView;
        textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));

        buttonInvio.setOnClickListener(v -> {
            // Assegnazione e pulizia della stringa contenuta nel box;
            String message = editTextMessage.getText().toString().trim();

            // Se la stringa non è vuota la si invia al server;
            if (!message.isEmpty()) {
                // Creazione di un nuovo thread per non interrompere il main thread durante l'invio della richiesta al server;
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

    private void handleTimeout(){
        // Gestione del timeout della richiesta: se dopo 2000ms il socket server è null (ovvero non ha risposto), si visualizza un messaggio di errore;
        final int timeout = 2000;
        new Handler().postDelayed(() -> {
            Runnable returnToMainMenu = () -> Utility.navigateTo(this, MainActivity.class);
            if(socket == null){
                // Visualizzazione di una dialog di errore;
                Utility.oneLineDialog(this,
                        // Titolo della dialog ("Non è stato possibile connettersi al server");
                        getString(R.string.connectionerror),
                        // Testo della prima opzione della dialog ("OK");
                        getString(R.string.ok),
                        // Testo della seconda opzione della dialog ("ANNULLA");
                        getString(R.string.cancel),
                        // Callback da eseguire alla pressione del primo tasto (Ritorna al menu principale);
                        returnToMainMenu,
                        // Callback da eseguire alla pressione del secondo tasto (Ritorna al menu principale);
                        returnToMainMenu,
                        // Callback da eseguire alla chiusura della dialog (Ritorna al menu principale);
                        returnToMainMenu);
            }
        }, timeout);
    }
    /**
     * Questo metodo connette il client al server, aggiorna le varie TextView con le informazioni del server e starta il listener dei messaggi;
     * @return void;
     */
    private void connectToServer(){
        try {
            // Connessione al server e creazione del socket;
            socket = new Socket(SERVER_IP, SERVER_PORT);

            System.out.printf("Connesso a %s:%d con successo!\n", SERVER_IP, SERVER_PORT);

            // Assegnazione dello stream output del socket;
            output = new DataOutputStream(socket.getOutputStream());

            // Assegnazione dello stream input del socket;
            input = new DataInputStream(socket.getInputStream());

            runOnUiThread(() -> {
                // Dialog di conferma;
                Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null);
                
                textViewIP.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP));
                textViewPort.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT));
            });

            // Il client si mette in ascolto del server;
            new Thread(this::messageListener).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * In questo metodo l'oggetto input si mette in ascolto del server e vengono stampati gli eventuali messaggi in entrata;
     * @return void;
     */
    private void messageListener(){
        // Si mette in ascolto del server fin tanto che l'oggetto input è definito;
        while (input != null) {
            try {
                // Si legge il messaggio in entrata;
                final String messageReceived = input.readUTF();

                if(messageReceived == null)
                    continue;

                System.out.printf("Messaggio ricevuto: %s.\n", messageReceived);
                Message message = (Message) Utility.jsonStringToObject(messageReceived, Message.class);

                runOnUiThread(() -> {
                    String finalMessage = String.format("%s: %s", message.getSender(), message.getContent());
                    textViewMessages.append(finalMessage);

                    // Scrolla la chat all'ultima riga ogni volta che si riceve un messaggio;
                    chatSV.fullScroll(View.FOCUS_DOWN);
                    editTextMessage.setText(new String());
                });
            } catch (IOException e) {
                disconnect();
                e.printStackTrace();
                return;
            }
        }
    }

    private void disconnect(){
        try{
            /*Si chiudono i socket e gli oggetti di input e output (Dopo aver effettuato
            un check per assicurarsi che non siano null per evitare la NullPointerException;*/
            if(socket != null)
                socket.close();

            if(output != null)
                output.close();

            if(input != null)
                input.close();

            this.runOnUiThread(() -> {
                Toast.makeText(this, this.getString(R.string.server_disconnected), Toast.LENGTH_SHORT).show();
                Utility.navigateTo(this, MainActivity.class);
            });
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Questo metodo invia un messaggio al server utilizzando l'oggetto che gestisce i messaggi in uscita;
     * @param content da inviare al server;
     * @throws IOException se si riscontra un errore durante l'invio di un messaggio al server;
     */
    private void sendMessage(String content) throws IOException{
        Message message = new Message(getUsername(), content);
        String jsonString = Utility.objectToJsonString(message);

        output.writeUTF(jsonString);
        output.flush();

        System.out.printf("%s inviato con successo.\n", jsonString);
    }

    /**
     * Override del metodo implementato in AppCompactActivity;
     * Chiude il socket e gli oggetti di input e output ogni volta che l'activity viene terminata o messa in pausa;
     * @return void;
     */
    @Override
    protected void onStop(){
        super.onStop();
        disconnect();
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
}
