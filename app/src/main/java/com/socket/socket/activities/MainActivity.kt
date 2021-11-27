package com.socket.socket.activities

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.socket.socket.R
import com.socket.socket.data.SharedPrefs
import com.socket.socket.engine.Server
import com.socket.socket.entity.Utente
import com.socket.socket.firebase.FirebaseClass
import com.socket.socket.utility.LoginUtility
import com.socket.socket.utility.Utility

class MainActivity : AppCompatActivity() {
    /**
     * Metodo che viene eseguito non appena l'activity principale viene creata;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPrefs.setContext(this)

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Si imposta il layout da visualizzare;
        setContentView(R.layout.activity_main)

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this)

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent))
        setListeners()
        if (!LoginUtility.isLoggedIn()) loginDialog()
    }

    /**
     * Questo metodo imposta i listener per ogni bottone presente nell'interfaccia grafica;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun setListeners() {
        val startServer = findViewById<Button?>(R.id.main_startserver)
        val joinServer = findViewById<Button?>(R.id.main_joinserver)
        val closeApp = findViewById<Button?>(R.id.main_closeapp)
        startServer.setOnClickListener { v: View? -> createServerDialog() }
        joinServer.setOnClickListener { v: View? ->
            Utility.navigateTo(
                this,
                JoinServer::class.java
            )
        }
        closeApp.setOnClickListener { v: View? -> onBackPressed() }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun createServerDialog() {
        // Si crea una nuova dialog;
        val dialog = Dialog(this)

        // Si imposta il layout della dialog;
        dialog.setContentView(R.layout.input_create_server)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val parentView = dialog.findViewById<ViewGroup?>(R.id.create_parent)
        val serverNameET: TextInputEditText = dialog.findViewById(R.id.create_name)
        val serverPWET: TextInputEditText = dialog.findViewById(R.id.create_pw)
        val confirmBtn = dialog.findViewById<Button?>(R.id.create_okDialog)
        val close = dialog.findViewById<ImageView?>(R.id.create_closeDialog)

        // Si imposta un listener al bottone di conferma;
        confirmBtn.setOnClickListener { v: View? ->
            val serverName = serverNameET.text.toString()
            val serverPW = serverPWET.text.toString()
            if (!FirebaseClass.isFirebaseStringValid(serverName) || serverName.isEmpty()) {
                dialog.dismiss()
                Utility.oneLineDialog(this, this.getString(R.string.invalidservername), null)
                return@setOnClickListener
            }
            dialog.dismiss()
            val i = Intent(this, Server::class.java)
            i.putExtra("serverName", serverName)
            i.putExtra("serverPW", Utility.getMd5(serverPW))
            this.startActivity(i)
        }
        close.setOnClickListener { v: View? ->
            // Chiude la dialog;
            dialog.dismiss()
        }

        // Si ridimensiona la dialog in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, parentView)
        dialog.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected fun loginDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.login_username_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val parentView = dialog.findViewById<ViewGroup?>(R.id.login_parent)
        val usernameInput: TextInputEditText = dialog.findViewById(R.id.login_UsernameInput)
        val passwordInput: TextInputEditText = dialog.findViewById(R.id.login_PasswordInput)
        val login = dialog.findViewById<Button?>(R.id.login_Confirm)
        val register = dialog.findViewById<Button?>(R.id.login_Register)
        val close = dialog.findViewById<ImageView?>(R.id.login_Close)
        login.setOnClickListener { v: View? ->
            val username = usernameInput.text.toString().trim { it <= ' ' }
            val password = passwordInput.text.toString().trim { it <= ' ' }
            val hashPassword = Utility.getMd5(password)
            if (!FirebaseClass.isFirebaseStringValid(username)) {
                Utility.oneLineDialog(this, this.getString(R.string.wrongcredentials), null)
                return@setOnClickListener
            }

            // Accedi all'account già esistente;
            FirebaseClass.getDBRef()?.child(username)?.get()
                ?.addOnCompleteListener { task: Task<DataSnapshot?>? ->
                    var entrato = false
                    if (task!!.isSuccessful()) {
                        for (d in task.result?.children!!) {
                            entrato = true
                            val value = d.value
                            val key: Any? = d.key
                            if (key == "password") {
                                if (hashPassword == value) {
                                    SharedPrefs.setUsername(username)
                                    SharedPrefs.setPassword(hashPassword)
                                    LoginUtility.updateEmail()
                                    Toast.makeText(
                                        applicationContext,
                                        this.getString(R.string.loginsuccess),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Utility.navigateTo(this, MainActivity::class.java)
                                    break
                                } else {
                                    Utility.oneLineDialog(
                                        this,
                                        this.getString(R.string.wrongcredentials),
                                        null
                                    )
                                }
                            }
                        }
                    }
                    if (!entrato) Utility.oneLineDialog(
                        this,
                        this.getString(R.string.wrongcredentials),
                        null
                    )
                    dialog.dismiss()
                }
        }
        register.setOnClickListener { v: View? ->
            // Nel caso in cui un utente ha inserito i dati di accesso nella pagina di login ma deve ancora registrarsi,
            // ricordiamo i dati inseriti nella pagina di register in modo tale che non dovrà reinserirli nuovamente;
            val previousUsernameInput = usernameInput.text.toString().trim { it <= ' ' }
            val previousPasswordInput = passwordInput.text.toString().trim { it <= ' ' }
            dialog.dismiss()
            registerDialog(previousUsernameInput, previousPasswordInput)
        }
        close.setOnClickListener { v: View? -> dialog.dismiss() }
        Utility.ridimensionamento(this, parentView)
        dialog.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected fun registerDialog(previousUsernameInput: String?, previousPasswordInput: String?) {
        val dialog = Dialog(this)
        dialog.setOnCancelListener { dialog1: DialogInterface? -> loginDialog() }
        dialog.setContentView(R.layout.register_username_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val parentView = dialog.findViewById<ViewGroup?>(R.id.register_parent)
        val usernameInput: TextInputEditText = dialog.findViewById(R.id.register_UsernameInput)
        val passwordInput: TextInputEditText = dialog.findViewById(R.id.register_PasswordInput)
        val emailInput: TextInputEditText = dialog.findViewById(R.id.register_EmailInput)
        val register = dialog.findViewById<Button?>(R.id.register_Confirm)
        val close = dialog.findViewById<ImageView?>(R.id.register_Close)
        usernameInput.setText(previousUsernameInput)
        passwordInput.setText(previousPasswordInput)
        Thread {
            runOnUiThread {
                register.setOnClickListener { v: View? ->
                    val username = usernameInput.text.toString().trim { it <= ' ' }
                    val password = passwordInput.text.toString().trim { it <= ' ' }
                    val hashPassword = Utility.getMd5(password)
                    val email = emailInput.text.toString().trim { it <= ' ' }
                        .replace(".", "_") // Firebase non accetta punti;
                    val usernameRequiredLength = 3
                    if (username.length < usernameRequiredLength) {
                        val message = this.getString(R.string.usernamelength)
                            .replace("{length}", usernameRequiredLength.toString())
                        Utility.oneLineDialog(this, message, null)
                        return@setOnClickListener
                    }
                    if (!FirebaseClass.isFirebaseStringValid(username)) {
                        Utility.oneLineDialog(this, this.getString(R.string.invaliddetails), null)
                        return@setOnClickListener
                    }
                    val tempEmail = email.replace("_", ".")
                    val isValidEmail =
                        !TextUtils.isEmpty(tempEmail) && Patterns.EMAIL_ADDRESS.matcher(tempEmail)
                            .matches()
                    if (!isValidEmail || !FirebaseClass.isFirebaseStringValid(email)) {
                        val message = this.getString(R.string.invaliddetails)
                        Utility.oneLineDialog(this, message, null)
                        return@setOnClickListener
                    }
                    val passwordRequiredLength = 6
                    if (password.length < passwordRequiredLength) {
                        val message = this.getString(R.string.passwordlength)
                            .replace("{length}", passwordRequiredLength.toString())
                        Utility.oneLineDialog(this, message, null)
                        return@setOnClickListener
                    }
                    FirebaseClass.getDBRef()?.get()
                        ?.addOnCompleteListener { task: Task<DataSnapshot?>? ->
                            var emailExists = false
                            outerLoop@ for (d in task?.getResult()?.getChildren()!!) {
                                for (row in d.children) {
                                    if (row.key == "email") {
                                        val emailChecked = row.value.toString()
                                        if (emailChecked == email) {
                                            emailExists = true
                                            break@outerLoop
                                        }
                                    }
                                }
                            }
                            if (emailExists) {
                                val message = this.getString(R.string.emailexists)
                                Utility.oneLineDialog(this, message, null)
                                return@addOnCompleteListener
                            }
                            FirebaseClass.getDBRef()
                                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.hasChild(username)) {
                                            Utility.oneLineDialog(
                                                this@MainActivity,
                                                this@MainActivity.getString(R.string.usernameexists),
                                                null
                                            )
                                        } else {
                                            val utente = Utente(username, hashPassword)
                                            FirebaseClass.addUserToFirebase(email, utente)
                                            SharedPrefs.setUsername(username)
                                            SharedPrefs.setPassword(hashPassword)
                                            SharedPrefs.setEmail(email)
                                            Toast.makeText(
                                                applicationContext,
                                                this@MainActivity.getString(R.string.registersuccess),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dialog.dismiss()
                                        }
                                    }

                                    override fun onCancelled(@NotNull error: DatabaseError) {}
                                })
                        }
                }
                close.setOnClickListener { v: View? -> dialog.dismiss() }
                Utility.ridimensionamento(this, parentView)
                dialog.show()
            }
        }.start()
    }

    /**
     * Override del metodo che viene invocato ogni volta che si preme il bottone "indietro";
     * Visualizza una dialog di conferma prima di chiudere l'applicazione;
     */
    override fun onBackPressed() {
        Utility.oneLineDialog(this, this.getString(R.string.confirmleave)) { finishAffinity() }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        val override = Configuration(newBase?.getResources()?.configuration)
        override.fontScale = 1.0f
        applyOverrideConfiguration(override)
    }
}