package com.example.manik.ytsapi;

import java.io.Serializable;

public class Cast implements Serializable{

    private String castName;

    private String castChar;

    public Cast(String name, String character) {
        castName = name;
        castChar = character;
    }

    public String getCastName() {
        return castName;
    }

    public String getCastChar() {
        return castChar;
    }

}
