package com.socket.socket.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.socket.socket.R
import com.socket.socket.UI.CDialog
import java.lang.reflect.Type
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Utility {
    fun objectToJsonString(`object`: Any?): String? {
        return Gson().toJson(`object`)
    }

    fun jsonStringToObject(jsonString: String?, x: Class<*>?): Any? {
//        String[] jsonStringTokenized = jsonString.split("\\{");
//        StringBuilder jsonStringResult = new StringBuilder();
//        System.out.println("Risultato " + jsonString);
//
//        for(int i = 1; i < jsonStringTokenized.length; i++)
//            jsonStringResult.append(String.format("{%s", jsonStringTokenized[i]));
//
//        System.out.println("Risultato " + jsonStringResult);
        println("$jsonString risultato")
        return Gson().fromJson(jsonString, x as Type?)
    }

    fun getMd5(input: String?): String? {
        return if (input.isEmpty()) input else try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val no = BigInteger(1, messageDigest)
            var hashtext = no.toString(16)
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }
            hashtext
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    fun navigateTo(c: AppCompatActivity?, cl: Class<*>?) {
        val i = Intent(c, cl)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        c.startActivity(i)
    }

    fun oneLineDialog(c: Context?, title: String?, callback: Runnable?) {
        CDialog(c as Activity?, title, callback).show()
    }

    fun oneLineDialog(
        c: Context?,
        title: String?,
        option1: String?,
        option2: String?,
        firstCallback: Runnable?,
        secondCallback: Runnable?,
        dismissCallback: Runnable?
    ) {
        CDialog(
            c as Activity?,
            title,
            option1,
            option2,
            firstCallback,
            secondCallback,
            dismissCallback
        ).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun enableTopBar(c: AppCompatActivity?) {
        val resId = c.getResources().getIdentifier("topbar", "drawable", c.getPackageName())
        c.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        c.getSupportActionBar().setBackgroundDrawable(c.getResources().getDrawable(resId))
        c.getSupportActionBar().setCustomView(R.layout.actionbar)
        val idS = "lefticon"
        val id = c.getResources().getIdentifier(idS, "id", c.getPackageName())
        c.findViewById<View?>(id).setOnClickListener { v: View? -> c.onBackPressed() }
    }

    // Ridimensiona i componenti in base alla dimensione dello schermo, NOTA: da utilizzare ogni qual volta si cambia la content view;
    fun ridimensionamento(activity: AppCompatActivity?, v: ViewGroup?) {
        val displayMetrics = DisplayMetrics()
        activity.getWindowManager().defaultDisplay.getMetrics(displayMetrics)
        val baseHeight = 1920.0
        val height = displayMetrics.heightPixels.toDouble()
        for (i in 0 until v.getChildCount()) {
            val vAtI = v.getChildAt(i)
            val curHeight = vAtI.layoutParams.height
            val curWidth = vAtI.layoutParams.width
            val rapporto = height / baseHeight
            if (curHeight > ViewGroup.LayoutParams.MATCH_PARENT) vAtI.layoutParams.height =
                (curHeight * rapporto) as Int
            if (curWidth > ViewGroup.LayoutParams.MATCH_PARENT) vAtI.layoutParams.width =
                (curWidth * rapporto) as Int
            if (vAtI is TextView) {
                val curSize = (vAtI as TextView).textSize as Int
                val newSize = (curSize * rapporto) as Int
                (vAtI as TextView).setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize.toFloat())
            }
            vAtI.requestLayout()
            if (vAtI is ViewGroup) {
                ridimensionamento(activity, vAtI as ViewGroup)
            }
        }
    }
}