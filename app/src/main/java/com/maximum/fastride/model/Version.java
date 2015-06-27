package com.maximum.fastride.model;

import java.util.Date;

/**
 * Created by Oleg Kleiman on 19-Jun-15.
 */
public class Version {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("major")
    private String major;
    public String getMajor() { return major; }

    @com.google.gson.annotations.SerializedName("minor")
    private String minor;
    public String getMinor() { return minor; }

    @com.google.gson.annotations.SerializedName("url")
    private String url;
    public String getURL() { return url; }

    @com.google.gson.annotations.SerializedName("released")
    private Date released;
    public Date getReleased() { return released; }
}
