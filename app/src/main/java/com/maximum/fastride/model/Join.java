package com.maximum.fastride.model;

import java.util.Date;

/**
 * Created by Oleg Kleiman on 14-Apr-15.
 */
public class Join {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("when_joined")
    private Date whenJoined;

    public Date getWhenJoined() {
        return whenJoined;
    }
    public void setWhenJoined(Date value) {
        whenJoined = value;
    }

    @com.google.gson.annotations.SerializedName("ridecode")
    private String rideCode;

    public String getRideCode() {
        return rideCode;
    }

    public void setRideCode(String value) {
        rideCode = value;
    }

    @com.google.gson.annotations.SerializedName("deviceid")
    private String deviceId;
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String value){
        deviceId = value;
    }
}

