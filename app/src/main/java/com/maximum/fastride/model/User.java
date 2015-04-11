package com.maximum.fastride.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.maximum.fastride.utils.Globals;

/**
 * Created by Oleg Kleiman on 11-Apr-15.
 */
public class User {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("first_name")
    private String first_name;
    public String getFirstName() {
        return first_name;
    }
    public void setFirstName(String value) {
        first_name = value;
    }

    @com.google.gson.annotations.SerializedName("last_name")
    private String last_name;
    public String getLastName() {
        return last_name;
    }
    public void setLastName(String value){
        last_name = value;
    }

    @com.google.gson.annotations.SerializedName("registration_id")
    private String registration_id;
    public String getRegistrationId() { return registration_id; }
    public void setRegistrationId(String value) { registration_id = value; }

    @com.google.gson.annotations.SerializedName("picture_url")
    private String picture_url;
    public String getPictureURL() { return this.picture_url; }
    public void setPictureURL(String value) { this.picture_url = value; }

    @com.google.gson.annotations.SerializedName("email")
    private String email;
    public String getEmail() { return this.email; }
    public void setEmail(String value) { this.email = value; }

    @com.google.gson.annotations.SerializedName("phone")
    private String phone;
    public String getPhone() { return this.phone; }
    public void setPhone(String value) { this.phone = value; }

    @com.google.gson.annotations.SerializedName("use_phone")
    private Boolean usePhone;
    public Boolean getUsePhone() { return this.usePhone; }
    public void setUsePhone(Boolean value) { this.usePhone = value; }

    public static User load(Context context) {

        User _user = new User();

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        _user.setFirstName(sharedPrefs.getString(Globals.FIRST_NAME_PREF, ""));
        _user.setLastName(sharedPrefs.getString(Globals.LAST_NAME_PREF, ""));
        _user.setRegistrationId(sharedPrefs.getString(Globals.REG_ID_PREF, ""));
        _user.setPictureURL(sharedPrefs.getString(Globals.PICTURE_URL_PREF, ""));
        _user.setEmail(sharedPrefs.getString(Globals.EMAIL_PREF, ""));
        _user.setPhone(sharedPrefs.getString(Globals.PHONE_PREF, ""));
        _user.setUsePhone(sharedPrefs.getBoolean(Globals.USE_PHONE_PFER, false));

        return _user;
    }

    public void save(Context context) {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(Globals.FIRST_NAME_PREF, this.getFirstName());
        editor.putString(Globals.LAST_NAME_PREF, this.getLastName());
        editor.putString(Globals.REG_ID_PREF, this.getRegistrationId());
        editor.putString(Globals.PICTURE_URL_PREF, this.getPictureURL());
        editor.putString(Globals.EMAIL_PREF, this.getEmail());
        editor.putString(Globals.PHONE_PREF, this.getPhone());
        editor.putBoolean(Globals.USE_PHONE_PFER, this.getUsePhone());

        editor.apply();
    }
}
