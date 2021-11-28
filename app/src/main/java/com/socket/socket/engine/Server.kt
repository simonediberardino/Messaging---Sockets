package com.socket.socket.engine

import android.content.Context
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.socket.socket.R
import com.socket.socket.entity.Message
import com.socket.socket.firebase.FirebaseClass
import com.socket.socket.utility.LoginUtility
import com.socket.socket.utility.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.*

public class Server : AppCompatActivity() {
    private var serverInstance: ServerInstance? = null
    // Porta del socket da creare (Default: 8080);
    public var SERVER_PORT = 8080

    // Indirizzo del socket da creare;
    private var SERVER_IP: String? = null
    private var instances: ArrayList<ClientInstance?>? = null
    private var serverSocket: ServerSocket? = null

    // Oggetti relativi all'interfaccia grafica;
    private var textViewIP: TextView? = null
    private var textViewPort: TextView? = null
    private var textViewMessages: TextView? = null
    private var editTextMessage: EditText? = null
    private var buttonInvio: ImageButton? = null
    private var chatSV: ScrollView? = null

    /**
     * Metodo che viene eseguito non appena l'activity viene creata;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Si imposta il layout da visualizzare;
        this.setContentView(R.layout.main_server)

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this)

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent))

        // Inizializzazione degli elementi dell'interfaccia grafica e dei listener;
        initializate()

        // Creazione di un nuovo thread per non interrompere il main thread durante la creazione del server socket;
        startSocketServer()
        Thread { listenClients() }.start()
    }

    /**
     * Questo metodo assegna ad ogni attributo dell'oggetto la relativa view dell'interfaccia grafica (utilizzando il loro ID) e setta gli eventuali listener;
     * @return void;
     */
    private fun initializate() {
        textViewIP = findViewById(R.id.server_info_address)
        textViewPort = findViewById(R.id.server_info_port)
        textViewMessages = findViewById(R.id.server_out_message)
        editTextMessage = findViewById(R.id.server_in_message)
        buttonInvio = findViewById(R.id.server_btn_send)
        chatSV = findViewById(R.id.server_scrollView)
        SERVER_IP = getLocalIpAddress()
        instances = ArrayList()

        // La box che permette di inviare i messaggi ai client è disattivata fino a quando un client non si connette al server;
        with(editTextMessage) { this?.setEnabled(false) }
        with(buttonInvio) {
            this?.setOnClickListener {
                val enteredText = editTextMessage
                // Assegnazione e pulizia della stringa contenuta nel box;
                val content = enteredText?.getText().toString().trim { it <= ' ' }
                if (!content.isEmpty()) {
                    // Creazione di un nuovo thread per non interrompere il main thread durante l'invio del messaggio ai client;
                    Thread {
                        try {
                            val message = Message(LoginUtility.getUsername(), content)
                            val jsonString = Utility.objectToJsonString(message)
                            for (i in instances!!) sendMessage(i, jsonString)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }.start()
                }
            }
        }
    }

    /**
     * Questo metodo crea e starta il server socket e aggiorna le TextView con le informazioni relative al socket;
     * @return void;
     */
    private fun startSocketServer() {
        try {
            // Si prelevano le informazioni inserite nella dialog nella activity precedente;
            val extras = intent.extras
            val serverName = extras?.getString("serverName")
            val serverPW = extras?.getString("serverPW")

            // Creazione del socket;
            serverSocket = ServerSocket(SERVER_PORT)
            serverInstance = ServerInstance(serverName, SERVER_IP, serverPW, SERVER_PORT)
            FirebaseClass.addServerToFirebase(serverInstance!!.getServerIP(), serverInstance)

            System.out.printf(
                "Socket creato con successo! Ascoltando sulla porta %d.\n",
                SERVER_PORT
            )

            runOnUiThread {
                textViewIP?.setText(
                    String.format(
                        "%s: %s",
                        getString(R.string.ipaddress),
                        SERVER_IP
                    )
                )
                textViewPort?.setText(String.format("%s: %s", getString(R.string.port), SERVER_PORT))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun listenClients() {
        var socket: Socket?
        while (serverSocket != null) {
            try {
                socket = serverSocket!!.accept()
                val clientInstance = ClientInstance(
                    DataOutputStream(socket.getOutputStream()),
                    DataInputStream(socket.getInputStream())
                )

                instances?.add(clientInstance)
                runOnUiThread {
                    // Dialog di conferma;
                    Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null)
                    editTextMessage?.setEnabled(true)
                }

                // Il server si mette in ascolto del client;
                Thread { messageListener(clientInstance) }.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * In questo metodo l'oggetto input si mette in ascolto del client e vengono stampati gli eventuali messaggi in entrata;
     * @return void;
     */
    private fun messageListener(instance: ClientInstance?) {
        println("Server in ascolto del client.")

        // Si mette in ascolto del client fin tanto che l'oggetto input è definito;
        while (instance?.getInput() != null) {
            try {
                // Si legge il messaggio in entrata;
                val jsonString = instance.getInput()!!.readUTF()
                // Lo si stampa sono nel caso in cui non sia null;
                if (jsonString != null) {
                    System.out.printf("Messaggio ricevuto: %s.\n", jsonString)
                    for (i in instances!!) sendMessage(i, jsonString)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Questo metodo invia un messaggio ai client utilizzando l'oggetto che gestisce i messaggi in uscita;
     * @param jsonString da inviare al server;
     * @throws IOException se si riscontra un errore durante l'invio di un messaggio ai client;
     */
    @Throws(IOException::class)
    private fun sendMessage(instance: ClientInstance?, jsonString: String?) {
        val message = Utility.jsonStringToObject(jsonString, Message::class.java) as Message
        instance?.getOutput()?.writeUTF(jsonString)
        instance?.getOutput()?.flush()

        System.out.printf("%s inviato con successo.\n", jsonString)

        runOnUiThread {
            val finalMessage = String.format("%s: %s", message.getSender(), message.getContent())
            textViewMessages?.append(finalMessage)

            // Scrolla la chat all'ultima riga ogni volta che si invia un messaggio;
            chatSV?.fullScroll(View.FOCUS_DOWN)
            editTextMessage?.setText(String())
        }
    }

    private fun closeServer() {
        try {
            for (i in instances!!) {
                i?.getOutput()?.close()
                i?.getInput()?.close()
            }

            serverSocket?.close()
            FirebaseClass.deleteFieldFirebase<Any?>(null, serverInstance?.getServerIP())
            println("Socket chiuso con successo.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Questo metodo restituisce l'indirizzo IP locale in formato stringa;
     * @return indirizzo IP locale;
     */
    private fun getLocalIpAddress(): String? {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }

    /**
     * Override del metodo implementato in AppCompactActivity;
     * Chiude il socket e gli oggetti di input e output ogni volta che l'activity viene terminata o messa in pausa;
     * @return void;
     */
    override fun onStop() {
        closeServer()
        super.onStop()
    }

    /**
     * Override del metodo che viene invocato ogni volta che si preme il bottone "indietro";
     * Visualizza una dialog di conferma prima di chiudere l'applicazione;
     */
    override fun onBackPressed() {
        Utility.oneLineDialog(this, this.getString(R.string.closesocket)) { super.onBackPressed() }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        val override = Configuration(newBase?.resources?.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
    }

    private class ClientInstance(
        private val output: DataOutputStream?,
        private val input: DataInputStream?
    ) {
        fun getOutput(): DataOutputStream? {
            return output
        }

        fun getInput(): DataInputStream? {
            return input
        }
    }

    class ServerInstance {
        private var serverName: String? = null
        private var serverIP: String? = null
        private var serverPW: String? = null
        private var serverPort = 0

        constructor() {}
        constructor(serverName: String?, serverIP: String?, serverPW: String?, serverPort: Int) {
            this.serverName = serverName
            this.serverIP = serverIP?.replace(".", "_")
            this.serverPW = serverPW
            this.serverPort = serverPort
        }

        fun getServerName(): String? {
            return serverName
        }

        fun getServerIP(): String? {
            return serverIP
        }

        fun getServerPW(): String? {
            return serverPW
        }

        fun getServerPort(): Int {
            return serverPort
        }
    }
}