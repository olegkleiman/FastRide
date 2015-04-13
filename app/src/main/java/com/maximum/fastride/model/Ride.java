package com.maximum.fastride.model;

import java.util.Date;

/**
 * Created by Oleg Kleiman on 13-Apr-15.
 */
public class Ride {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("ridecode")
    private String rideCode;
    public String getRideCode() {
        return rideCode;
    }
    public void setRideCode(String value) { rideCode = value; }

    @com.google.gson.annotations.SerializedName("created")
    private Date created;
    public Date getCreated() { return created; }
    public void setCreated(Date value) { created = value; }

}
