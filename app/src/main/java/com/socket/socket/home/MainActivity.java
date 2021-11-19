package com.socket.socket.home;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.InetAddresses;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.socket.socket.data.SharedPrefs;
import com.socket.socket.engine.Client;
import com.socket.socket.R;
import com.socket.socket.engine.Server;
import com.socket.socket.entity.Utente;
import com.socket.socket.firebase.FirebaseClass;
import com.socket.socket.utility.LoginUtility;
import com.socket.socket.utility.Utility;

import static com.socket.socket.firebase.FirebaseClass.isFirebaseStringValid;
import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity{
    /**
     * Metodo che viene eseguito non appena l'activity principale viene creata;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        SharedPrefs.setContext(this);

        // Si imposta l'applicazione in fullscreen;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Si imposta il layout da visualizzare;
        setContentView(R.layout.activity_main);

        // Si abilita la topbar personalizzata;
        Utility.enableTopBar(this);

        // Si ridimensionano tutti le view dell'interfaccia grafica in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, findViewById(R.id.parent));

        setListeners();

        if(!LoginUtility.isLoggedIn())
            loginDialog();
    }

    /**
     * Questo metodo imposta i listener per ogni bottone presente nell'interfaccia grafica;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setListeners(){
        Button startServer = findViewById(R.id.main_startserver);
        Button joinServer = findViewById(R.id.main_joinserver);
        Button closeApp = findViewById(R.id.main_closeapp);

        startServer.setOnClickListener(v -> {
            Utility.navigateTo(this, Server.class);
        });

        joinServer.setOnClickListener(v -> {
            joinServerDialog();
        });

        closeApp.setOnClickListener(v -> {
            this.onBackPressed();
        });
    }

    /**
     * Questo metodo crea una input dialog che permette di inserire le informazioni del server a cui il client dovrà connettersi;
     * @return void;
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void joinServerDialog(){
        // Si crea una nuova dialog;
        Dialog dialog = new Dialog(this);

        // Si imposta il layout della dialog;
        dialog.setContentView(R.layout.input_join_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.input_parent);
        TextInputEditText addressInput = dialog.findViewById(R.id.input_addressDialog);
        TextInputEditText portInput = dialog.findViewById(R.id.input_portDialog);
        Button confirmBtn = dialog.findViewById(R.id.input_okDialog);

        ImageView close = dialog.findViewById(R.id.input_closeDialog);

        // Si ridimensiona la dialog in base alla dimensione dello schermo;
        Utility.ridimensionamento(this, parentView);

        // Si imposta un listener al bottone di conferma;
        confirmBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString();

            int port;
            try{
                port = Integer.parseInt(portInput.getText().toString());
            }catch(Exception e){
                // Se la porta non è un numero intero si visualizza un messaggio di errore;
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            // Se l'indirizzo IP non è un indirizzo valido si visualizza un messaggio di errore;
            if(!InetAddresses.isNumericAddress(address)){
                Utility.oneLineDialog(this, getString(R.string.invalidaddress), null);
                return;
            }

            // Se la porta non appartiene al range di porte disponibili visualizza un messaggio di errore;
            final int PORT_MIN = 1024, PORT_MAX = 65535;
            if(port < PORT_MIN || port > PORT_MAX){
                Utility.oneLineDialog(this, getString(R.string.invalidportnumber), null);
                return;
            }

            // Starta una nuova activity passandole le informazioni inserite nella dialog;
            Intent i = new Intent(this, Client.class);
            i.putExtra("address", address);
            i.putExtra("port", port);

            this.startActivity(i);
        });

        close.setOnClickListener(v -> {
            // Chiude la dialog;
            dialog.dismiss();
        });

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void loginDialog(){
        Dialog dialog = new Dialog(this);

        dialog.setCancelable(false);

        dialog.setContentView(R.layout.login_username_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.login_parent);
        TextInputEditText usernameInput = dialog.findViewById(R.id.login_UsernameInput);
        TextInputEditText passwordInput = dialog.findViewById(R.id.login_PasswordInput);
        Button login = dialog.findViewById(R.id.login_Confirm);
        Button register = dialog.findViewById(R.id.login_Register);
        ImageView close = dialog.findViewById(R.id.login_Close);

        login.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String hashPassword = Utility.getMd5(password);

            if(!isFirebaseStringValid(username)){
                Utility.oneLineDialog(this, this.getString(R.string.wrongcredentials), null);
                return;
            }

            // Accedi all'account già esistente;
            FirebaseClass.getDBRef().child(username).get().addOnCompleteListener(task -> {
                boolean entrato = false;

                if(task.isSuccessful()){
                    for(DataSnapshot d: task.getResult().getChildren()){
                        entrato = true;

                        Object value = d.getValue();
                        Object key = d.getKey();

                        if(key.equals("password")){
                            if(hashPassword.equals(value)){
                                SharedPrefs.setUsername(username);
                                SharedPrefs.setPassword(hashPassword);
                                LoginUtility.updateEmail();

                                Toast.makeText(getApplicationContext(), this.getString(R.string.loginsuccess), Toast.LENGTH_SHORT).show();
                                Utility.navigateTo(this, MainActivity.class);
                                break;
                            }else{
                                Utility.oneLineDialog(this, this.getString(R.string.wrongcredentials), null);
                            }
                        }
                    }
                }

                if(!entrato)
                    Utility.oneLineDialog(this, this.getString(R.string.wrongcredentials), null);

                dialog.dismiss();
            });
        });

        register.setOnClickListener(v -> {
            // Nel caso in cui un utente ha inserito i dati di accesso nella pagina di login ma deve ancora registrarsi,
            // ricordiamo i dati inseriti nella pagina di register in modo tale che non dovrà reinserirli nuovamente;
            String previousUsernameInput = usernameInput.getText().toString().trim();
            String previousPasswordInput = passwordInput.getText().toString().trim();

            dialog.dismiss();
            registerDialog(previousUsernameInput, previousPasswordInput);
        });

        close.setOnClickListener(v -> dialog.dismiss());

        Utility.ridimensionamento(this, parentView);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void registerDialog(String previousUsernameInput, String previousPasswordInput){
        Dialog dialog = new Dialog(this);

        dialog.setOnCancelListener(dialog1 -> loginDialog());
        dialog.setContentView(R.layout.register_username_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup parentView = dialog.findViewById(R.id.register_parent);
        TextInputEditText usernameInput = dialog.findViewById(R.id.register_UsernameInput);
        TextInputEditText passwordInput = dialog.findViewById(R.id.register_PasswordInput);
        TextInputEditText emailInput = dialog.findViewById(R.id.register_EmailInput);
        Button register = dialog.findViewById(R.id.register_Confirm);
        ImageView close = dialog.findViewById(R.id.register_Close);

        usernameInput.setText(previousUsernameInput);
        passwordInput.setText(previousPasswordInput);

        new Thread(() -> {
            runOnUiThread(() -> {
                register.setOnClickListener(v -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String hashPassword = Utility.getMd5(password);
                    String email = emailInput.getText().toString().trim().replace(".", "_"); // Firebase non accetta punti;

                    final Integer usernameRequiredLength = 3;
                    if(username.length() < usernameRequiredLength){
                        String message = this.getString(R.string.usernamelength).replace("{length}", usernameRequiredLength.toString());
                        Utility.oneLineDialog(this, message, null);
                        return;
                    }

                    if(!isFirebaseStringValid(username)){
                        Utility.oneLineDialog(this, this.getString(R.string.invaliddetails), null);
                        return;
                    }

                    String tempEmail = email.replace("_", ".");
                    boolean isValidEmail = (!TextUtils.isEmpty(tempEmail) && Patterns.EMAIL_ADDRESS.matcher(tempEmail).matches());
                    if(!isValidEmail || !isFirebaseStringValid(email)){
                        String message = this.getString(R.string.invaliddetails);
                        Utility.oneLineDialog(this, message, null);
                        return;
                    }

                    final Integer passwordRequiredLength = 6;
                    if(password.length() < passwordRequiredLength){
                        String message = this.getString(R.string.passwordlength).replace("{length}", passwordRequiredLength.toString());
                        Utility.oneLineDialog(this, message, null);
                        return;
                    }

                    FirebaseClass.getDBRef().get().addOnCompleteListener(task -> {
                        boolean emailExists = false;

                        outerLoop: for(DataSnapshot d: task.getResult().getChildren()){
                            for(DataSnapshot row : d.getChildren()){
                                if(row.getKey().equals("email")){
                                    String emailChecked = valueOf(row.getValue());
                                    if(emailChecked.equals(email)) {
                                        emailExists = true;
                                        break outerLoop;
                                    }
                                }
                            }
                        }

                        if(emailExists){
                            String message = this.getString(R.string.emailexists);
                            Utility.oneLineDialog(this, message, null);
                            return;
                        }

                        FirebaseClass.getDBRef().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if(snapshot.hasChild(username)){
                                    Utility.oneLineDialog(MainActivity.this, MainActivity.this.getString(R.string.usernameexists), null);
                                }else{
                                    Utente utente = new Utente(username, hashPassword);
                                    FirebaseClass.addToFirebase(email, utente);

                                    SharedPrefs.setUsername(username);
                                    SharedPrefs.setPassword(hashPassword);
                                    SharedPrefs.setEmail(email);

                                    Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.registersuccess),Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }

                            @Override public void onCancelled(@NonNull @NotNull DatabaseError error) {}
                        });
                    });
                });

                close.setOnClickListener(v -> dialog.dismiss());

                Utility.ridimensionamento(this, parentView);
                dialog.show();
            });
        }).start();
    }

    /**
     * Override del metodo che viene invocato ogni volta che si preme il bottone "indietro";
     * Visualizza una dialog di conferma prima di chiudere l'applicazione;
     */
    @Override
    public void onBackPressed() {
        Utility.oneLineDialog(this, this.getString(R.string.confirmleave), this::finishAffinity);
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
