package com.socket.socket.entity;

public class Utente{
    private final String email;
    private final String password;

    public Utente(String email, String password){
        this.email = email;
        this.password = password;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }
}
