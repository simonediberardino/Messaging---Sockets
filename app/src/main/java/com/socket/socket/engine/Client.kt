package com.socket.socket.engine

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.socket.socket.R
import com.socket.socket.activities.MainActivity
import com.socket.socket.entity.Message
import com.socket.socket.utility.LoginUtility
import com.socket.socket.utility.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

//@TODO: Aggiungere lista server;
class Client : AppCompatActivity() {
    companion object {
        // Porta del server a cui connettersi;
        val SERVER_PORT: Int = 80
    }

    // Indirizzo IP del server a cui connettersi;
    private var SERVER_IP: String? = null
    private var SERVER_IP_FB: String? = null

    // Oggetto di tipo DataOutputStream che gestirà i messaggi in uscita;
    private var output: DataOutputStream? = null

    // Oggetto di tipo DataInputStream che gestirà i messaggi in entrata;
    private var input: DataInputStream? = null
    private var socket: Socket? = null

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
        val extras = intent.extras

        // Si prelevano le informazioni inserite nella dialog nella activity precedente;
        val passwordEntered = extras?.getString("pw")
        SERVER_IP = extras?.getString("address")
        SERVER_IP_FB = SERVER_IP?.replace(".", "_")

        // Inizializzazione degli elementi dell'interfaccia grafica e dei listener;
        initializate()
        handleTimeout()

        // Creazione di un nuovo thread per non interrompere il main thread durante la connessione al server;
        Thread { connectToServer() }.start()
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

        // Si scrive l'indirizzo IP del server nella TextView;
        with(textViewIP) { this?.setText(String.format("%s: %s", getString(R.string.ipaddress), SERVER_IP)) }

        // Si scrive la porta del server nella TextView;
        with(textViewPort) {
            this?.setText(String.format("%s: %s", getString(R.string.port),
                Companion.SERVER_PORT
            ))
        }

        with(buttonInvio) {
            this?.setOnClickListener {
                val enteredText = editTextMessage
                // Assegnazione e pulizia della stringa contenuta nel box;
                val message = enteredText?.getText().toString().trim { it <= ' ' }

                // Se la stringa non è vuota la si invia al server;
                if (!message.isEmpty()) {
                    // Creazione di un nuovo thread per non interrompere il main thread durante l'invio della richiesta al server;
                    Thread {
                        try {
                            sendMessage(message)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }.start()
                }
            }
        }
    }

    private fun handleTimeout() {
        // Gestione del timeout della richiesta: se dopo 2000ms il socket server è null (ovvero non ha risposto), si visualizza un messaggio di errore;
        val timeout = 2000
        Handler().postDelayed({
            val returnToMainMenu = Runnable { Utility.navigateTo(this, MainActivity::class.java) }
            if (socket == null) {
                // Visualizzazione di una dialog di errore;
                Utility.oneLineDialog(
                    this,  // Titolo della dialog ("Non è stato possibile connettersi al server");
                    getString(R.string.connectionerror),  // Testo della prima opzione della dialog ("OK");
                    getString(R.string.ok),  // Testo della seconda opzione della dialog ("ANNULLA");
                    getString(R.string.cancel),  // Callback da eseguire alla pressione del primo tasto (Ritorna al menu principale);
                    returnToMainMenu,  // Callback da eseguire alla pressione del secondo tasto (Ritorna al menu principale);
                    returnToMainMenu,  // Callback da eseguire alla chiusura della dialog (Ritorna al menu principale);
                    returnToMainMenu
                )
            }
        }, timeout.toLong())
    }

    /**
     * Questo metodo connette il client al server, aggiorna le varie TextView con le informazioni del server e starta il listener dei messaggi;
     * @return void;
     */
    private fun connectToServer() {
        try {
            // Connessione al server e creazione del socket;
            socket = Socket(SERVER_IP, Companion.SERVER_PORT)
            System.out.printf("Connesso a %s:%d con successo!\n", SERVER_IP, Companion.SERVER_PORT)

            // Assegnazione dello stream output del socket;
            output = DataOutputStream(socket!!.getOutputStream())

            // Assegnazione dello stream input del socket;
            input = DataInputStream(socket!!.getInputStream())
            runOnUiThread {

                // Dialog di conferma;
                Utility.oneLineDialog(this, getString(R.string.connectsuccessfully), null)
                textViewIP?.setText(
                    String.format(
                        "%s: %s",
                        getString(R.string.ipaddress),
                        SERVER_IP
                    )
                )
                textViewPort?.setText(String.format("%s: %s", getString(R.string.port),
                    SERVER_PORT
                ))
            }

            // Il client si mette in ascolto del server;
            Thread { messageListener() }.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * In questo metodo l'oggetto input si mette in ascolto del server e vengono stampati gli eventuali messaggi in entrata;
     * @return void;
     */
    private fun messageListener() {
        // Si mette in ascolto del server fin tanto che l'oggetto input è definito;
        while (input != null) {
            try {
                // Si legge il messaggio in entrata;
                val messageReceived = input!!.readUTF() ?: continue
                System.out.printf("Messaggio ricevuto: %s.\n", messageReceived)
                val message =
                    Utility.jsonStringToObject(messageReceived, Message::class.java) as Message
                runOnUiThread {
                    val finalMessage =
                        String.format("%s: %s", message.getSender(), message.getContent())
                    textViewMessages?.append(finalMessage)

                    // Scrolla la chat all'ultima riga ogni volta che si riceve un messaggio;
                    chatSV?.fullScroll(View.FOCUS_DOWN)
                    editTextMessage?.setText(String())
                }
            } catch (e: IOException) {
                disconnect()
                e.printStackTrace()
                return
            }
        }
    }

    private fun disconnect() {
        try {
            /*Si chiudono i socket e gli oggetti di input e output (Dopo aver effettuato
            un check per assicurarsi che non siano null per evitare la NullPointerException;*/
            socket?.close()
            output?.close()
            input?.close()
            runOnUiThread {
                Toast.makeText(
                    this,
                    this.getString(R.string.server_disconnected),
                    Toast.LENGTH_SHORT
                ).show()
                Utility.navigateTo(this, MainActivity::class.java)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Questo metodo invia un messaggio al server utilizzando l'oggetto che gestisce i messaggi in uscita;
     * @param content da inviare al server;
     * @throws IOException se si riscontra un errore durante l'invio di un messaggio al server;
     */
    @Throws(IOException::class)
    private fun sendMessage(content: String?) {
        val message = Message(LoginUtility.getUsername(), content)
        val jsonString = Utility.objectToJsonString(message)
        output?.writeUTF(jsonString)
        output?.flush()
        System.out.printf("%s inviato con successo.\n", jsonString)
    }

    /**
     * Override del metodo implementato in AppCompactActivity;
     * Chiude il socket e gli oggetti di input e output ogni volta che l'activity viene terminata o messa in pausa;
     * @return void;
     */
    override fun onStop() {
        super.onStop()
        disconnect()
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
        val override = Configuration(newBase?.getResources()?.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
    }
}