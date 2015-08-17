package com.maximum.fastride.utils;

import android.app.PendingIntent;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.Intent;
import android.database.Observable;
import android.os.Build;

import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

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

    public static  String CASCADE_URL = "http://maximum.azurewebsites.net/data/lbpcascades/lbpcascade_frontalface.xml";
    private static String CASCADE_PATH;
    public static void initCascadePath(Context ctx) {
        String DEFAULT_CASCADE_NAME = "lbpcascade_frontalface.xml";
        File file = new File(ctx.getFilesDir(), DEFAULT_CASCADE_NAME);
        synchronized (lock ) {
            CASCADE_PATH = file.getAbsolutePath();
        }
    }
    public static String getCascadePath (Context ctx) {
        if( CASCADE_PATH == null || CASCADE_PATH.isEmpty() )
            initCascadePath(ctx);

        return CASCADE_PATH;
    }
    public static void setCascadePath(String path) {
        synchronized (lock) {
            CASCADE_PATH = path;
        }
    }

    /**
     * Timeouts (in millis) for startAdvertising and startDiscovery.  At the end of these time
     * intervals the app will silently stop advertising or discovering.
     *
     * To set advertising or discovery to run indefinitely, use 0L where timeouts are required.
     */
    public static final long TIMEOUT_ADVERTISE = 1000L * 30L;
    public static final long TIMEOUT_DISCOVER = 1000L * 30L;

    // Geofences
    public static final HashMap<String, LatLng> FWY_AREA_LANDMARKS = new HashMap<String, LatLng>();
//    static {
//        FWY_AREA_LANDMARKS.put("GOOGLE", new LatLng(32.080341,34.780639));
//    }
    public static ArrayList<Geofence> GEOFENCES = new ArrayList<Geofence>();
    public static PendingIntent GeofencePendingIntent;

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 2;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 100;
    public static final int GEOFENCE_LOITERING_DELAY = 60000; // 1 min
    public static final int GEOFENCE_RESPONSIVENESS = 5000; // 5 sec

    private static final Object lock = new Object();
    private static boolean inGeofenceArea;
    public static boolean isInGeofenceArea() {
        synchronized (lock) {
            return inGeofenceArea;
        }
    }
    public static void setInGeofenceArea(boolean value) {
        synchronized (lock) {
            inGeofenceArea = value;
        }
    }

    private static Object lock2 = new Object();
    private static String MONITOR_STATUS;
    public static String getMonitorStatus() {
        synchronized (lock2) {
            return MONITOR_STATUS;
        }
    }
    public static void setMonitorStatus(String value) {
        synchronized (lock2) {
            MONITOR_STATUS = value;
        }
    }

    private static Object lock3 = new Object();
    private static boolean _REMIND_GEOFENCE_ENTRANCE;
    public static void setRemindGeofenceEntrance() {
        synchronized ( lock3 ) {
            _REMIND_GEOFENCE_ENTRANCE = true;
        }
    }
    public static Boolean getRemindGeofenceEntrance() {
        synchronized ( lock3 ) {
            return _REMIND_GEOFENCE_ENTRANCE;
        }
    }

}
