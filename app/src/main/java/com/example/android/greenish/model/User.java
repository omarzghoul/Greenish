package com.example.android.greenish.model;

import com.example.android.greenish.MarkerInfo;

import java.util.List;

public class User {

    public String firstName;
    public String email;
    public int watering = 0;
    public int plant = 0;

    public User() {
    }

    public User (String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.email = email;
    }

    public User (String firstName, int plant, int watering) {
        this.firstName = firstName;
        this.plant = plant;
        this.watering = watering;
    }

}