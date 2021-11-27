package com.socket.socket.entity

class Utente {
    private var username: String? = null
    private var password: String? = null

    constructor() {}
    constructor(username: String?, password: String?) {
        this.username = username
        this.password = password
    }

    fun getUsername(): String? {
        return username
    }

    fun getPassword(): String? {
        return password
    }
}