package com.maximum.fastride.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Oleg on 09-Jun-15.
 */
public class wamsUtils {

    private static final String LOG_TAG = "FR.WAMS";

    static public void sync(MobileServiceClient wamsClient, String... tables) {

        try {

            MobileServiceSyncContext syncContext = wamsClient.getSyncContext();

            if (!syncContext.isInitialized()) {

               for(String table : tables) {

                   Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                   SQLiteLocalStore localStore  = new SQLiteLocalStore(wamsClient.getContext(),
                                                                        table, null, 1);

                   switch( table ) {
                       case "rides": {
                           tableDefinition.put("id", ColumnDataType.String);
                           tableDefinition.put("ridecode", ColumnDataType.String);
                           tableDefinition.put("driverid", ColumnDataType.String);
                           tableDefinition.put("created", ColumnDataType.Date);
                           tableDefinition.put("carnumber", ColumnDataType.String);
                           tableDefinition.put("picture_url", ColumnDataType.String);
                           tableDefinition.put("approved", ColumnDataType.Boolean);
                           tableDefinition.put("__deleted", ColumnDataType.Boolean);
                           tableDefinition.put("__version", ColumnDataType.String);
                       }
                       break;

                       case "gfences": {
                           tableDefinition.put("id", ColumnDataType.String);
                           tableDefinition.put("lat", ColumnDataType.Number);
                           tableDefinition.put("lon", ColumnDataType.Number);
                           tableDefinition.put("when_updated", ColumnDataType.Date);
                           tableDefinition.put("label", ColumnDataType.String);
                           tableDefinition.put("isactive", ColumnDataType.Boolean);
                           tableDefinition.put("__deleted", ColumnDataType.Boolean);
                           tableDefinition.put("__version", ColumnDataType.String);
                       }
                       break;
                   }

                   localStore.defineTable(table, tableDefinition);
                   syncContext.initialize(localStore, null).get();
                }

            }

        } catch(MobileServiceLocalStoreException | InterruptedException | ExecutionException ex) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }
    }

    static public MobileServiceClient init(Context context) throws MalformedURLException {

        MobileServiceClient wamsClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    context);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userID = sharedPrefs.getString(Globals.USERIDPREF, "");
        MobileServiceUser wamsUser = new MobileServiceUser(userID);

        String token = sharedPrefs.getString(Globals.WAMSTOKENPREF, "");
        // According to this article (http://www.thejoyofcode.com/Setting_the_auth_token_in_the_Mobile_Services_client_and_caching_the_user_rsquo_s_identity_Day_10_.aspx)
        // this should be JWT token, so use WAMS_TOKEN
        wamsUser.setAuthenticationToken(token);

        wamsClient.setCurrentUser(wamsUser);

        return  wamsClient;

    }
}
