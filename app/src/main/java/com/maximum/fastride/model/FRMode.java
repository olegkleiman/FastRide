package com.maximum.fastride.model;

/**
 * Created by Oleg Kleiman on 28-May-15.
 */
public class FRMode {
    private String text;
    public void setName(String val) {
        text = val;
    }
    public String getName() {
        return text;
    }

    private int imageId;
    public void setImageId(int val){
        imageId = val;
    }
    public int getimageId() {
        return imageId;
    }

}
