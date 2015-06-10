package com.maximum.fastride;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.maximum.fastride.model.GFence;
import com.maximum.fastride.services.GeofenceErrorMessages;
import com.maximum.fastride.services.GeofenceTransitionsIntentService;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.wamsUtils;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.util.Map;
import java.util.Random;

/**
 * Created by Oleg on 09-Jun-15.
 */
public class BaseActivityWithGeofences extends BaseActivity
                                        implements ResultCallback<Status> {

    private static final String LOG_TAG = "FR.GeoFences";
    private MobileServiceSyncTable<GFence> mGFencesSyncTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    protected void initGeofences(final MobileServiceClient wamsClient) {

        final ResultCallback resultCallback = this;

        if( mGFencesSyncTable == null )
            mGFencesSyncTable = wamsClient.getSyncTable("gfences", GFence.class);

        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {

                MobileServiceList<GFence> gFences = null;
                try {

                    wamsUtils.sync(wamsClient, "gfences");

                    Query pullQuery = wamsClient.getTable(GFence.class).where();
                    gFences = mGFencesSyncTable.read(pullQuery).get();

                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
                }

                if( gFences == null )
                    return null;

                try {
                    // After getting landmark coordinates from WAMS,
                    // the steps for dealing with geofences are following:
                    // 1. populate FWY_AREA_LANDMARKS in Globals
                    // 2. based on this hashmap, populate GEOFENCES in Globals
                    // 3. create GeofencingRequest request based on GEOFENCES list
                    // 4. define pending intent for geofences transitions
                    // 5. add geofences to Google API service

                    Random r = new Random();
                    String gFenceName = "gf_";
                    for (GFence _gFence : gFences) {
                        gFenceName += r.nextInt(100);
                        double lat = _gFence.getLat();
                        double lon = _gFence.getLat();
                        LatLng latLng = new LatLng(lat, lon);
                        Globals.FWY_AREA_LANDMARKS.put(gFenceName, latLng);
                    }

                    for (Map.Entry<String, LatLng> entry : Globals.FWY_AREA_LANDMARKS.entrySet()) {

                        Globals.GEOFENCES.add(new Geofence.Builder()
                                .setRequestId(entry.getKey())
                                        // Set the circular region of this geofence.
                                .setCircularRegion(
                                        entry.getValue().latitude,
                                        entry.getValue().longitude,
                                        Globals.GEOFENCE_RADIUS_IN_METERS
                                )

                                        // Set the expiration duration of the geofence. This geofence gets automatically
                                        // removed after this period of time.
                                .setExpirationDuration(Globals.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                        // Set the transition types of interest. Alerts are only generated for these
                                        // transition. We track entry and exit transitions here.
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                        Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build());
                    }

                    GeofencingRequest geoFencingRequest = getGeofencingRequest();
                    if( geoFencingRequest != null ) {
                        LocationServices.GeofencingApi.addGeofences(
                                getGoogleApiClient(), // from base activity
                                // The GeofenceRequest object.
                                geoFencingRequest,
                                // A pending intent that that is reused when calling removeGeofences(). This
                                // pending intent is used to generate an intent when a matched geofence
                                // transition is observed.
                                getGeofencePendingIntent()
                        ).setResultCallback(resultCallback); // Result processed in onResult().
                    }

                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
                }

                return null;
            }
        }.execute();


    }

    @Override
    public void onPause() {
        super.onPause();

        LocationServices.GeofencingApi.removeGeofences(
                getGoogleApiClient(),
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    private GeofencingRequest getGeofencingRequest() {

        try {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

            // Add the geofences to be monitored by geofencing service.
            builder.addGeofences(Globals.GEOFENCES);

            // Return a GeofencingRequest.
            return builder.build();
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
            return null;
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if ( Globals.GeofencePendingIntent != null) {
            return Globals.GeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
//
//            Toast.makeText(this,getString(R.string.geofences_added),
//                            Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(LOG_TAG, errorMessage);
        }
    }

}
