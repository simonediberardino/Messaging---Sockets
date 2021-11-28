package com.socket.socket.data

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object SharedPrefs {
    private var context: Context? = null
    private val SHARED_PREFS: String? = "sharedPrefs"
    private val USERNAME_ID: String? = "USERNAME"
    private val PASSWORD_ID: String? = "PASSWORD"
    private val EMAIL_ID: String? = "EMAIL"
    private val USERNAME_DEFAULT: String? = "null"
    private val PASSWORD_DEFAULT: String? = "null"
    private val EMAIL_DEFAULT: String? = "null"
    fun setUsername(username: String?) {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(USERNAME_ID, username)
        editor?.apply()
    }

    fun setEmail(email: String?) {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(EMAIL_ID, email)
        editor?.apply()
    }

    fun getEmail(): String? {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(EMAIL_ID, EMAIL_DEFAULT)
    }

    fun getUsername(): String? {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(USERNAME_ID, USERNAME_DEFAULT)
    }

    fun setPassword(password: String?) {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(PASSWORD_ID, password)
        editor?.apply()
    }

    fun getPassword(): String? {
        val sharedPreferences = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(PASSWORD_ID, PASSWORD_DEFAULT)
    }

    fun setContext(context: Context?) {
        SharedPrefs.context = context
    }
}