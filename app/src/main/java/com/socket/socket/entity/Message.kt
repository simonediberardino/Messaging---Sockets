package com.socket.socket.entity


class Message(private var sender: String?, private var content: String?) {
    fun getSender(): String? {
        return sender
    }

    fun getContent(): String? {
        return content
    }

    init {
        this.content = this.content?.trim()
    }
}