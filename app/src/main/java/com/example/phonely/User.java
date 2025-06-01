package com.example.phonely;

public class User {
    public String email;
    public String role;
    public String imageUrl; // NEW

    public User() {} // Required for Firebase

    public User(String email, String role, String imageUrl) {
        this.email = email;
        this.role = role;
        this.imageUrl = imageUrl;
    }
}

