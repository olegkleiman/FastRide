package com.maximum.fastride;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.maximum.fastride.adapters.WiFiPeersAdapter2;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;
import com.maximum.fastride.model.GFence;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.services.GeofenceErrorMessages;
import com.maximum.fastride.services.GeofenceTransitionsIntentService;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.ConflictResolvingSyncHandler;
import com.maximum.fastride.utils.FloatingActionButton;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.GroupOwnerSocketHandler;
import com.maximum.fastride.utils.IMessageTarget;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.IRefreshable;
import com.maximum.fastride.utils.ITrace;
import com.maximum.fastride.utils.WiFiUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DriverRoleActivity extends BaseActivity
                implements ITrace,
                           IMessageTarget,
                           Handler.Callback,
                           IRefreshable,
                           IRecyclerClickListener,
                           WiFiUtil.IPeersChangedListener,
                           WifiP2pManager.PeerListListener,
                           WifiP2pManager.ConnectionInfoListener,
                           ResultCallback<Status> // for geofences' callback
{

    private static final String LOG_TAG = "FR.Driver";

    Ride mCurrentRide;

    public static MobileServiceClient wamsClient;
    private MobileServiceTable<Ride> ridesTable;
    private MobileServiceSyncTable<GFence> mGFencesSyncTable;
    private SQLiteLocalStore mLocalStore;

    RecyclerView mPeersRecyclerView;
    WiFiPeersAdapter2 mPeersAdapter;
    public List<WifiP2pDeviceUser> peers = new ArrayList<>();

    WiFiUtil wifiUtil;
    private Handler handler = new Handler(this);
    public Handler getHandler() {
        return handler;
    }

    TextView mTxtStatus;

    String mUserID;
    String mCarNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_role);

        setupUI(getString(R.string.title_activity_driver_role), "");
        wamsInit();

        List<String> _cars = new ArrayList<>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> carsSet = sharedPrefs.getStringSet(Globals.CARS_PREF, new HashSet<String>());
        if( carsSet != null ) {
            Iterator<String> iterator = carsSet.iterator();
            while (iterator.hasNext()) {
                String carNumber = iterator.next();
                _cars.add(carNumber);
            }
        }

        String[] cars = new String[_cars.size()];
        cars = _cars.toArray(cars);

        if( cars.length == 0 ) {
            new MaterialDialog.Builder(this)
                    .title(R.string.edit_car_dialog_caption2)
                    .content(R.string.edit_car_dialog_text)
                    .autoDismiss(true)
                    .cancelable(false)
                    .positiveText(getString(R.string.edit_car_button_title2))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent intent = new Intent(getApplicationContext(),
                                    SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();
        }else if( cars.length > 1) {

            new MaterialDialog.Builder(this)
                    .title(R.string.edit_car_dialog_caption1)
                    .autoDismiss(true)
                    .cancelable(false)
                    .items(cars)
                    .positiveText(getString(R.string.edit_car_button_title))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog,
                                                View view,
                                                int which,
                                                CharSequence text) {
                            mCarNumber = text.toString();
                        }
                    })
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent intent = new Intent(getApplicationContext(),
                                    SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .show();
        } else {
            mCarNumber = cars[0];
        }


        wifiUtil = new WiFiUtil(this);
        wifiUtil.deletePersistentGroups();

        mUserID = sharedPrefs.getString(Globals.USERIDPREF, "");

        // This will publish the service in DNS-SD and start serviceDiscovery()
        wifiUtil.startRegistrationAndDiscovery(this, mUserID);

    }

    @Override
    protected void setupUI(String title, String subTitle){
        super.setupUI(title, subTitle);

        View fab = findViewById(R.id.submit_ride_button);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FloatingActionButton _fab = (FloatingActionButton)fab;
            _fab.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_done));
            _fab.setBackgroundColor(getResources().getColor(R.color.ColorAccent));
        } else {
            fab.setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    int diameter = getResources().getDimensionPixelSize(R.dimen.diameter);
                    outline.setOval(0, 0, diameter, diameter);
                }
            });
            fab.setClipToOutline(true);
        }

        mTxtStatus = (TextView)findViewById(R.id.txtStatus);
        mPeersRecyclerView = (RecyclerView)findViewById(R.id.recyclerViewPeers);
        mPeersRecyclerView.setHasFixedSize(true);
        mPeersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPeersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mPeersAdapter = new WiFiPeersAdapter2(this, R.layout.peers_header, peers);
        mPeersRecyclerView.setAdapter(mPeersAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        wifiUtil.registerReceiver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        wifiUtil.unregisterReceiver();

        LocationServices.GeofencingApi.removeGeofences(
                getGoogleApiClient(),
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    @Override
    protected void onStop() {
        wifiUtil.removeGroup();
        super.onStop();
    }

    public void onDebug(View view){
        LinearLayout layout = (LinearLayout) findViewById(R.id.debugLayout);
        int visibility = layout.getVisibility();
        if( visibility == View.VISIBLE )
            layout.setVisibility(View.GONE);
        else
            layout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case Globals.TRACE_MESSAGE:
                Bundle bundle = msg.getData();
                String strMessage = bundle.getString("message");
                trace(strMessage);
                break;

            case Globals.MESSAGE_READ:
                byte[] buffer = (byte[] )msg.obj;
                strMessage = new String(buffer);
                trace(strMessage);
                break;

        }

        return true;
    }

    private void wamsInit( ){
        try {
            wamsClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    this);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            String userID = sharedPrefs.getString(Globals.USERIDPREF, "");
            MobileServiceUser wamsUser = new MobileServiceUser(userID);

            String token = sharedPrefs.getString(Globals.WAMSTOKENPREF, "");
            // According to this article (http://www.thejoyofcode.com/Setting_the_auth_token_in_the_Mobile_Services_client_and_caching_the_user_rsquo_s_identity_Day_10_.aspx)
            // this should be JWT token, so use WAMS_TOKEN
            wamsUser.setAuthenticationToken(token);

            wamsClient.setCurrentUser(wamsUser);

            geoFencesInit();

            ridesTable = wamsClient.getTable("rides", Ride.class);

            new AsyncTask<Void, Void, Void>() {

                Exception mEx;

                @Override
                protected void onPostExecute(Void result){

                    if( mEx == null ) {
                        TextView txtRideCode = (TextView) findViewById(R.id.txtRideCode);
                        txtRideCode.setText(mCurrentRide.getRideCode());
                    } else {
                        Toast.makeText(DriverRoleActivity.this,
                                mEx.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected Void doInBackground(Void... voids) {

                    try {

                        Ride ride = new Ride();
                        ride.setCreated(new Date());
                        ride.setCarNumber(mCarNumber);
                        mCurrentRide = ridesTable.insert(ride).get();

                    } catch(ExecutionException | InterruptedException ex ) {
                        mEx = ex;
                        Log.e(LOG_TAG, ex.getMessage());
                    }

                    return null;
                }
            }.execute();
        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }
    }

    private void geoFencesInit(){
        if( wamsClient == null )
            return;

        try {

            mLocalStore = new SQLiteLocalStore(wamsClient.getContext(),
                                                "gfences", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
            MobileServiceSyncContext syncContext = wamsClient.getSyncContext();
            if (!syncContext.isInitialized()) {
                Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                tableDefinition.put("id", ColumnDataType.String);
                tableDefinition.put("lat", ColumnDataType.Number);
                tableDefinition.put("lon", ColumnDataType.Number);
                tableDefinition.put("when_updated", ColumnDataType.Date);
                tableDefinition.put("__deleted", ColumnDataType.Boolean);
                tableDefinition.put("__version", ColumnDataType.String);

                mLocalStore.defineTable("gfences", tableDefinition);
                syncContext.initialize(mLocalStore, null).get();
            }

            mGFencesSyncTable = wamsClient.getSyncTable("gfences", GFence.class);

            refreshGeofences();

        } catch( MobileServiceLocalStoreException| ExecutionException |InterruptedException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }

    }

    private void refreshGeofences() {

        if( mGFencesSyncTable == null )
            return;

        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {

                try {

                    Query pullQuery = wamsClient.getTable(GFence.class).where();

                    mGFencesSyncTable.purge(pullQuery);
                    mGFencesSyncTable.pull(pullQuery).get();

                    MobileServiceList<GFence> gFences = mGFencesSyncTable.read(pullQuery).get();

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
                        ).setResultCallback(DriverRoleActivity.this); // Result processed in onResult().
                    }

                } catch (InterruptedException | ExecutionException ex) {
                    Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
                }

                return null;
            }
        }.execute();


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

    public void onButtonSubmitRide(View v){

        if( mCurrentRide == null )
            return;

        mCurrentRide.setApproved(true);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    ridesTable.update(mCurrentRide).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver_role, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_debug) {
            onDebug(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    //
    // Implementation of IPeerClickListener
    //
    public void clicked(View view, int position) {

        try {

            WifiP2pDeviceUser device = peers.get(position);

            if (device.status == WifiP2pDevice.AVAILABLE) {
                wifiUtil.connectToDevice(device, 0);
            } else {
                Toast.makeText(this,
                        "Device should be in available state",
                        Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception ex){
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    //
    // Implementation of IRefreshable
    //
    public void refresh() {
        peers.clear();
        mPeersAdapter.notifyDataSetChanged();

        final ImageButton btnRefresh = (ImageButton)findViewById(R.id.btnRefresh);
        btnRefresh.setVisibility(View.GONE);
        final ProgressBar progress_refresh = (ProgressBar)findViewById(R.id.progress_refresh);
        progress_refresh.setVisibility(View.VISIBLE);

        wifiUtil.startRegistrationAndDiscovery(this, mUserID);

        getHandler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        btnRefresh.setVisibility(View.VISIBLE);
                        progress_refresh.setVisibility(View.GONE);
                    }
                },
                5000);
    }

    //
    // Implementation of WifiP2pManager.PeerListListener
    // Used to synchronize peers statuses after connection

    @Override
    public void onPeersAvailable(WifiP2pDeviceList list){
        for(WifiP2pDevice device : list.getDeviceList()) {
            WifiP2pDeviceUser d = new WifiP2pDeviceUser(device);
            d.setUserId(mUserID);
            mPeersAdapter.updateItem(d);
        }
    }

    //
    // Implementation of WifiP2pManager.ConnectionInfoListener
    //

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        TextView txtMe = (TextView)findViewById(R.id.txtMe);
        Thread handler = null;

        wifiUtil.requestPeers(this);

         /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * ServerAsyncTask}
         */
        if (p2pInfo.isGroupOwner) {
            txtMe.setText("ME: GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());
            //new WiFiUtil.ServerAsyncTask(this).execute();
            try {
                handler = new GroupOwnerSocketHandler(getHandler());
                handler.start();
                trace("Server socket opened.");
            } catch (IOException e){
                trace("Failed to create a server thread - " + e.getMessage());
            }
        } else {
            txtMe.setText("ME: NOT GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());
            handler = new ClientSocketHandler(
                    this.getHandler(),
                    p2pInfo.groupOwnerAddress,
                    this,
                    "!!!Message from DRIVER!!!");
            handler.start();
            trace("Client socket opened.");
        }

        // Optionally may request group info
        //mManager.requestGroupInfo(mChannel, this);


    }

    @Override
    public void trace(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String current = mTxtStatus.getText().toString();
                mTxtStatus.setText(current + "\n" + status);
            }
        });
    }

    @Override
    public void alert(final String strIntent) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if( which == DialogInterface.BUTTON_POSITIVE ) {
                    startActivity(new Intent(strIntent));
                }
            }};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable Wi-Fi on your device?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    //
    // Implementations of WifiUtil.IPeersChangedListener
    //
    @Override
    public void add(final WifiP2pDeviceUser device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPeersAdapter.add(device);
                mPeersAdapter.notifyDataSetChanged();
            }
        });
    }
}
