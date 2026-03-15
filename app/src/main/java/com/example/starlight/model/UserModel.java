package com.example.starlight.model;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    // Constructor for signup
    public UserModel(String name, String email, String password) {
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }
}