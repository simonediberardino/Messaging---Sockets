package com.socket.socket.home;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.socket.socket.R;
import com.socket.socket.data.SharedPrefs;
import com.socket.socket.entity.Utente;
import com.socket.socket.firebase.FirebaseClass;
import com.socket.socket.utility.LoginUtility;
import com.socket.socket.utility.Utility;

import java.util.ArrayList;

import static com.socket.socket.firebase.FirebaseClass.isFirebaseStringValid;
import static java.lang.String.valueOf;

public class LoginActivity extends AppCompatActivity{

}
