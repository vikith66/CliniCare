package com.example.hospitalappointment;

public class User {
    public String name;
    public String email;
    public String phone;
    public String password;

    public User() { } // Needed for Firebase

    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }
}
