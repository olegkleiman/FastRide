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

    @com.google.gson.annotations.SerializedName("carnumber")
    private String carNumber;
    public String getCarNumber() { return carNumber; }
    public void setCarNumber(String value) { carNumber = value; }

    @com.google.gson.annotations.SerializedName("approved")
    private Boolean approved;
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean value) { approved = value; }
}
