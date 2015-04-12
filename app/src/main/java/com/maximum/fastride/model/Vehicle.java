package com.maximum.fastride.model;

/**
 * Created by Oleg Kleiman on 11-Apr-15.
 */
public class Vehicle {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("lp_number")
    private String number;
    public String getNumber() {
        return number;
    }
    public void setNumber(String value) {
        number = value;
    }

    @com.google.gson.annotations.SerializedName("lp_number")
    private String userId;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String value) { userId = value; }

}
