package com.socket.socket.entity;

public class Utente{
    private String username;
    private String password;

    public Utente(){}

    public Utente(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
