package com.maximum.fastride.model;

import java.util.Date;

/**
 * Created by Oleg Kleiman on 08-Jun-15.
 */
public class GFence {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("lat")
    private float lat;
    public float getLat() { return lat; }
    public void setLat(float val) { lat = val; }

    @com.google.gson.annotations.SerializedName("lon")
    private float lon;
    public float getLon() { return lon; }
    public void setLon(float val) { lon = val; }

    @com.google.gson.annotations.SerializedName("when_updated")
    private Date whenUpdated;
    public Date getWhenUpdated() { return whenUpdated; }
    public void setWhenUpdated(Date value) { whenUpdated = value; }

    @com.google.gson.annotations.SerializedName("label")
    private String label;
    public String getLabel() { return label; }
//    public void setLabel(String value) { label = value; }

    @com.google.gson.annotations.SerializedName("isactive")
    private Boolean _isActive;
    public boolean isActive() { return _isActive; }
//    public void setActive(boolean value) { _isActive = value; }
}
