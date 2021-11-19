package com.socket.socket.entity;

public class Message{
    public String sender, content;

    public Message(String sender, String content){
        this.sender = sender;
        this.content = content + "\n";
    }

    public String getSender(){
        return sender;
    }

    public String getContent(){
        return content;
    }
}
