package com.maximum.fastride.utils;

import android.content.ContentQueryMap;
import android.content.Context;
import android.content.Intent;
import android.database.Observable;
import android.os.Build;

import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.maps.model.LatLng;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Oleg Kleiman on 11-Apr-15.
 */
public class Globals {

    public static final boolean DEVELOPER_MODE = false;

    private static class DManClassFactory {

        static DrawMan drawMan;

        static DrawMan getDrawMan(){
            if( drawMan == null )
                return new DrawMan();
            else
                return drawMan;
        }
    }
    public static final DrawMan drawMan = DManClassFactory.getDrawMan();

    public static float PICTURE_CORNER_RADIUS = 20;
    public static float PICTURE_BORDER_WIDTH = 4;

    static final public int SERVER_PORT = 4545;
    static final public int SOCKET_TIMEOUT = 5000;
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String TXTRECORD_PROP_USERNAME = "username";
    public static final String TXTRECORD_PROP_PORT = "port";
    public static final String SERVICE_INSTANCE = "_wififastride";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final String USERIDPREF = "userid";
    public static final String CARS_PREF = "cars";
    public static final String TOKENPREF = "accessToken";
    public static final String WAMSTOKENPREF = "wamsToken";

    public static final String FB_PROVIDER = "Facebook";
    public static final String FB_PROVIDER_FOR_STORE = "Facebook:";
    public static final String GOOGLE_PROVIDER_FOR_STORE = "Google:";
    public static final String MS_PROVIDER_FOR_STORE = "MS:";
    public static final String TWITTER_PROVIDER_FOR_STORE = "Twitter:";
    public static final String PLATFORM = "Android" + Build.VERSION.SDK_INT;

    // 'Project number' of project 'FastRide"
    // See Google Developer Console -> Billing & settings
    // https://console.developers.google.com/project/third-apex-91200/settings
    public static final String SENDER_ID = "1041824085053";

    public static final String WAMS_URL = "https://fastride.azure-mobile.net/";
    public static final String WAMS_API_KEY = "omCudOMCUJgIGbOklMKYckSiGKajJU91";

    public static final String FB_USERNAME_PREF = "username";
    public static final String FB_LASTNAME__PREF = "lastUsername";
    public static final String REG_PROVIDER_PREF = "registrationProvider";

    public static final String FIRST_NAME_PREF = "firstname";
    public static final String LAST_NAME_PREF = "lastname";
    public static final String REG_ID_PREF = "regid";
    public static final String PICTURE_URL_PREF = "pictureurl";
    public static final String EMAIL_PREF = "email";
    public static final String PHONE_PREF = "phone";
    public static final String USE_PHONE_PFER = "usephone";

    // Driver/passenger 'chat' messages
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int TRACE_MESSAGE = 0x400 + 3;

    // Geofences

    public static final HashMap<String, LatLng> FWY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {
        // Home. Petach-Tikwa
        FWY_AREA_LANDMARKS.put("Home", new LatLng(32.0746, 34.872));

        // Googleplex.
        FWY_AREA_LANDMARKS.put("GOOGLE", new LatLng(37.422611,-122.0840577));
    }

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
}
