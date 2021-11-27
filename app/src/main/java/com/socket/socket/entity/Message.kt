package com.socket.socket.entity

class Message(var sender: String?, content: String?) {
    var content: String?
    fun getSender(): String? {
        return sender
    }

    fun getContent(): String? {
        return content
    }

    init {
        this.content = """
            $content
            
            """.trimIndent()
    }
}