package com.example.phonely;

public class PhoneModel {

    public String name;
    public String imageUrl;
    public String price;
    public String description;

    public PhoneModel() {} // Needed for Firebase

    public PhoneModel(String name, String imageUrl, String price, String description) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.description = description;
    }

}
