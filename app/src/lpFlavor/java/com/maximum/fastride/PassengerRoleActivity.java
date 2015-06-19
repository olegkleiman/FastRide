package com.maximum.fastride;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Connections;
import com.maximum.fastride.adapters.WiFiPeersAdapter2;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.GroupOwnerSocketHandler;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.IRefreshable;
import com.maximum.fastride.utils.ITrace;
import com.maximum.fastride.utils.WiFiUtil;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PassengerRoleActivity extends BaseActivityWithGeofences
    implements ITrace,
        Handler.Callback,
        IRecyclerClickListener,
        IRefreshable,
        WiFiUtil.IPeersChangedListener,
        WifiP2pManager.ConnectionInfoListener,
        GoogleApiClient.ConnectionCallbacks,
        Connections.EndpointDiscoveryListener,
        Connections.MessageListener{

    private static final String LOG_TAG = "FR.Passenger";

    MobileServiceTable<Join> joinsTable;

    WiFiUtil wifiUtil;
    WiFiPeersAdapter2 mDriversAdapter;
    public List<WifiP2pDeviceUser> drivers = new ArrayList<>();

    TextView mTxtStatus;
    String mUserID;

    Boolean mDriversShown;
    TextView mTxtMonitorStatus;

    private android.os.Handler handler = new android.os.Handler(this);
    public android.os.Handler getHandler() {
        return handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        setupUI(getString(R.string.title_activity_passenger_role), "");
        wamsInit();

        // Keep device awake when discovering by Nearby host
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTxtStatus = (TextView)findViewById(R.id.txtStatusPassenger);

        joinsTable = getMobileServiceClient().getTable("joins", Join.class);

        wifiUtil = new WiFiUtil(this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserID = sharedPrefs.getString(Globals.USERIDPREF, "");


        new Thread() {
            @Override
            public void run(){

                try{

                    while (true) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                    String message = Globals.isInGeofenceArea() ?
                                            Globals.getMonitorStatus() :
                                            getString(R.string.geofence_outside);

                                    mTxtMonitorStatus.setText(message);

                            }
                        });

                        Thread.sleep(1000);
                    }
                }
                catch(InterruptedException ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }

            }
        }.start();

        // This will start serviceDiscovery
        // for (hopefully) already published service
        wifiUtil.startRegistrationAndDiscovery(this, mUserID);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(LOG_TAG, "onConfigurationChanged");
    }

    protected void setupUI(String title, String subTitle){

        super.setupUI(title, subTitle);

        RecyclerView driversRecycler = (RecyclerView)findViewById(R.id.recyclerViewDrivers);
        driversRecycler.setHasFixedSize(true);
        driversRecycler.setLayoutManager(new LinearLayoutManager(this));
        driversRecycler.setItemAnimator(new DefaultItemAnimator());

        mDriversAdapter = new WiFiPeersAdapter2(this, R.layout.drivers_header, drivers);
        driversRecycler.setAdapter(mDriversAdapter);

        mDriversShown = false;

        mTxtMonitorStatus = (TextView)findViewById(R.id.status_monitor);
        Globals.setMonitorStatus(getString(R.string.geofence_outside));

    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

//        /**
//         * Begin discovering devices advertising Nearby Connections, if possible.
//         */
//        String serviceId = getString(R.string.service_id);
//
//        if (!isConnectedToNetwork()) {
//            Log.e(LOG_TAG, "startDiscovery: not connected to WiFi network");
//            return;
//        }
//
//        Nearby.Connections.startDiscovery(getGoogleApiClient(),
//                serviceId,
//                0, //Globals.TIMEOUT_DISCOVER,
//                this)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.d(LOG_TAG, "startDiscovery:onResult: SUCCESS");
//                        } else {
//                            int statusCode = status.getStatusCode();
//                            Log.e(LOG_TAG, "startDiscovery:onResult: FAILURE");
//                        }
//                    }
//                });

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
    }

    @Override
    protected void onStop() {
        wifiUtil.removeGroup();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_passenger, menu);

        if( mDriversShown ) {
            menu.findItem(R.id.action_code).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_debug) {
            onDebug(null);
            return true;
        } else if( id == R.id.action_code) {
            showSubmitCodeDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSubmitCodeDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.ride_code_title)
                .content(R.string.ride_code_dialog_content)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER)
                .inputMaxLength(5)
                .input(R.string.ride_code_hint,
                        R.string.ride_code_refill,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String rideCode = input.toString();
                                onSubmitCode(rideCode);
                            }
                        }

                ).show();
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

        String strMessage;

        switch (msg.what) {
            case Globals.TRACE_MESSAGE:
                Bundle bundle = msg.getData();
                strMessage = bundle.getString("message");
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

    public void onSubmit() {
        onSubmitCode("");
    }

    public void onSubmitCode(final String rideCode){
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final View v = findViewById(R.id.passenger_internal_layout);

        new AsyncTask<Void, Void, Void>() {

            Exception mEx;
            String mRideCode;

            @Override
            protected void onPostExecute(Void result){

                if( mEx != null ) {

                    String msg = mEx.getMessage();
                    String[] tokens = msg.split(":");
                    if(tokens.length > 1)
                        msg = tokens[1];

                    Snackbar snackbar =
                            Snackbar.make(v, msg, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.code_retry_action,
                                        new View.OnClickListener(){
                                        @Override
                                        public void onClick(View v){
                                            showSubmitCodeDialog();
                                        }
                            });
                    snackbar.setActionTextColor(getResources().getColor(R.color.white));
                    //snackbar.setDuration(8000);
                    snackbar.show();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try{
                    Join _join  = new Join();
                    _join.setWhenJoined(new Date());
                    _join.setRideCode(rideCode);
                    _join.setDeviceId(android_id);

                    joinsTable.insert(_join).get();

                } catch(ExecutionException| InterruptedException ex ) {
                    mEx = ex;
                    Log.e(LOG_TAG, ex.getMessage());
                }

                return null;
            }
        }.execute();
    }

    //
    // Implementation of EndpointDiscoveryListener
    //
    @Override
    public void onEndpointFound(final String endpointId,
                                String deviceId,
                                String serviceId,
                                final String endpointName) {
        Log.d(LOG_TAG, "onEndpointFound:" + endpointId + ":" + endpointName);
        WifiP2pDeviceUser device = new WifiP2pDeviceUser(endpointName,
                                                         endpointId);
        mDriversAdapter.add(device);
        mDriversAdapter.notifyDataSetChanged();
        connectTo(endpointId, endpointName);
    }

    @Override
    public void onEndpointLost(String endpointId) {
        Log.d(LOG_TAG, "onEndpointLost:" + endpointId);
    }

    private void connectTo(String endpointId, final String endpointName) {
        Log.d(LOG_TAG, "connectTo:" + endpointId + ":" + endpointName);

        String myName = null;
        byte[] myPayload = null;
        Nearby.Connections.sendConnectionRequest(getGoogleApiClient(),
                myName, endpointId, myPayload,
                new Connections.ConnectionResponseCallback() {
                    @Override
                    public void onConnectionResponse(String endpointId, Status status,
                                                     byte[] bytes) {
                        Log.d(LOG_TAG, "onConnectionResponse:" + endpointId + ":" + status);
                        if (status.isSuccess()) {
                            Log.d(LOG_TAG, "onConnectionResponse: " + endpointName + " SUCCESS");
                        }else {
                            Log.d(LOG_TAG, "onConnectionResponse: " + endpointName + " FAILURE");
                        }
                    }},
                this);
    }

    //
    // Implementation of Connections.MessageListener
    //
    @Override
    public void onMessageReceived(String endpointId,
                                  byte[] payload,
                                  boolean isReliable) {
        // A message has been received from a remote endpoint.
        Log.d(LOG_TAG, "onMessageReceived:" + endpointId + ":" + new String(payload));
    }

    @Override
    public void onDisconnected(String endpointId) {
        Log.d(LOG_TAG, "onDisconnected:" + endpointId);
    }

    //
    // Implementation of WifiP2pManager.ConnectionInfoListener
    //

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo p2pInfo) {

        TextView txtMe = (TextView)findViewById(R.id.txtPassengerMe);
        Thread handler = null;

        if (p2pInfo.isGroupOwner) {
            txtMe.setText("ME: GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());
            try {
                handler = new GroupOwnerSocketHandler(this.getHandler());
                handler.start();
            } catch (IOException e){
                trace("Failed to create a server thread - " + e.getMessage());
            }

        } else {
            txtMe.setText("ME: NOT GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());

            handler = new ClientSocketHandler(
                    this.getHandler(),
                    p2pInfo.groupOwnerAddress,
                    this,
                    "!!!Message from PASSENGER!!!");
            handler.start();
            trace("Client socket opened.");

//            android.os.Handler h = new android.os.Handler();
//
//            Runnable r = new Runnable() {
//                @Override
//                public void run() {
//
//                    new WiFiUtil.ClientAsyncTask(context, p2pInfo.groupOwnerAddress,
//                                        "From client").execute();
//                }
//            };
//
//            h.postDelayed(r, 2000); // let to server to open the socket in advance

        }

    }

    // WiFiDirectBroadcastReceiver.IWiFiStateChanges implementation

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
    public void alert(String message, final String actionIntent) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if( which == DialogInterface.BUTTON_POSITIVE ) {
                    startActivity(new Intent(actionIntent));
                }
            }};

        new AlertDialogWrapper.Builder(this)
                .setTitle(message)
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
    }

    //
    // Implementations of WifiUtil.IPeersChangedListener
    //
    @Override
    public void add(final WifiP2pDeviceUser device) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDriversAdapter.add(device);
                mDriversAdapter.notifyDataSetChanged();

                // remove 'type code' menu item
                mDriversShown = true;
                invalidateOptionsMenu();
            }
        });
    }

    //
    // Implementation of IPeerClickListener
    //
    public void clicked(View view, int position) {

        WifiP2pDeviceUser driverDevice = drivers.get(position);

        if( !Globals.isInGeofenceArea() ) {
            new MaterialDialog.Builder(this)
                    .title(R.string.geofence_outside_title)
                    .content(R.string.geofence_outside)
                    .positiveText(R.string.geofence_positive_answer)
                    .negativeText(R.string.geofence_negative_answer)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Globals.setRemindGeofenceEntrance();
                        }
                    })
                    .show();
    }

    else

    {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    onSubmit();
                }
            }
        };

        String message = getString(R.string.passenger_confirm) + driverDevice.deviceName;

        new AlertDialogWrapper.Builder(this)
                .setTitle(message)
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
    }
}

        //
        // Implementation of IRefreshable
        //
        public void refresh() {
        drivers.clear();
        mDriversAdapter.notifyDataSetChanged();

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

}
