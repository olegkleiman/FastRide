package com.maximum.fastride;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.net.Uri;
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
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.maximum.fastride.adapters.WiFiPeersAdapter2;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.services.GeofenceErrorMessages;
import com.maximum.fastride.utils.BLEUtil;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.GroupOwnerSocketHandler;
import com.maximum.fastride.utils.IMessageTarget;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.IRefreshable;
import com.maximum.fastride.utils.ITrace;
import com.maximum.fastride.utils.WAMSVersionTable;
import com.maximum.fastride.utils.WiFiUtil;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DriverRoleActivity extends BaseActivityWithGeofences
                implements ITrace,
                           IMessageTarget,
                           Handler.Callback,
                           IRefreshable,
                           IRecyclerClickListener,
                           WiFiUtil.IPeersChangedListener,
                           WifiP2pManager.PeerListListener,
                           WifiP2pManager.ConnectionInfoListener,
                           GoogleApiClient.ConnectionCallbacks,
                           WAMSVersionTable.IVersionMismatchListener,
                           ResultCallback<Status> // for geofences' callback
{

    private static final String LOG_TAG = "FR.Driver";

    Ride mCurrentRide;

    private MobileServiceTable<Ride> ridesTable;

    RecyclerView mPeersRecyclerView;
    WiFiPeersAdapter2 mPeersAdapter;
    public List<WifiP2pDeviceUser> peers = new ArrayList<>();

    WiFiUtil wifiUtil;
    private Handler handler = new Handler(this);
    public Handler getHandler() {
        return handler;
    }

    TextView mTxtStatus;
    TextView mTxtMonitorStatus;

    String mUserID;
    String mCarNumber;

    TextToSpeech mTTS;

    final int WIFI_CONNECT_REQUEST = 1;// request code for starting WiFi connection
                                        // handled  in onActivityResult

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_role);

        mTTS = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener(){

                    @Override
                    public void onInit(int status) {
                        if( status != TextToSpeech.ERROR ) {
                            mTTS.setLanguage(Locale.US);
                        }
                    }
                });

        setupUI(getString(R.string.title_activity_driver_role), "");
        wamsInit(true);

        // Keep device awake when advertising fow Wi-Fi Direct
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ridesTable = getMobileServiceClient().getTable("rides", Ride.class);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BLEUtil bleUtil = new BLEUtil(this);
            Boolean bleRes = bleUtil.startAdvertise();
        }

        // This will publish the service in DNS-SD and start serviceDiscovery()
        wifiUtil.startRegistrationAndDiscovery(this, mUserID);

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

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(LOG_TAG, "onConfigurationChanged");
    }

    @Override
    protected void setupUI(String title, String subTitle){
        super.setupUI(title, subTitle);

        ImageView imgListen = (ImageView)findViewById(R.id.img_listen);
        imgListen.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                TextView txtRideCode = (TextView) findViewById(R.id.txtRideCode);

                mTTS.speak(txtRideCode.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

            }
        });

        View fab = findViewById(R.id.submit_ride_button);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            FloatingActionButton _fab = (FloatingActionButton)fab;
//            _fab.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_done));
//            _fab.setBackgroundColor(getResources().getColor(R.color.ColorAccent));
            FloatingActionButton _fab = (FloatingActionButton)fab;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) _fab.getLayoutParams();
            p.setMargins(0, 0, 0, 0); // get rid of margins since shadow area is now the margin
            _fab.setLayoutParams(p);
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

        mTxtMonitorStatus = (TextView)findViewById(R.id.status_monitor);
        Globals.setMonitorStatus(getString(R.string.geofence_outside));
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
    }

    //
    // Implementation of IVersionMismatchListener
    //
    public void mismatch(int major, int minor, final String url){
        try {
            new MaterialDialog.Builder(getApplicationContext())
                    .title(getString(R.string.new_version_title))
                    .content(getString(R.string.new_version_conent))
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            //intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .show();
        } catch (MaterialDialog.DialogException e) {
            // better that catch the exception here would be use handle to send events the activity
        }
    }

    public void match() {

    }

    public void connectionFailure(Exception ex) {

        if( ex != null ) {

            View v = findViewById(R.id.drawer_layout);
            Snackbar.make(v, ex.getMessage(), Snackbar.LENGTH_LONG);
        }

    }


    @Override
    public void onResume() {
            super.onResume();

        wifiUtil.registerReceiver(this);
    }

    @Override
    public void onPause() {

        wifiUtil.unregisterReceiver();

        if( mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onPause();

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

    public void onCamera(View view) {
        //Intent intent = new Intent(this, CameraActivity.class);
        Intent intent = new Intent(this, CameraCVActivity.class);
        startActivity(intent);
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
        } else if( id == R.id.action_camera ) {
            onCamera(null);
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
    // Implementation of Connections.MessageListener
    //
    public void onMessageReceived(String endpointId, byte[] payload, boolean isReliable) {
        Log.d(LOG_TAG, "onMessageReceived:" + endpointId + ":" + new String(payload));
    }

    public void onDisconnected(String endpointId){
        Log.d(LOG_TAG, "onDisconnected:" + endpointId);
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
    public void alert(String message, final String actionIntent) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if( which == DialogInterface.BUTTON_POSITIVE ) {
                    startActivityForResult(new Intent(actionIntent), WIFI_CONNECT_REQUEST);
                }
            }};

        new AlertDialogWrapper.Builder(this)
                .setTitle(message)
                .setNegativeButton(R.string.no, dialogClickListener)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if( requestCode == WIFI_CONNECT_REQUEST) {
            // if( resultCode == RESULT_OK ) {
            // How to distinguish between successful connection
            // and just pressing back from there?
                wamsInit(true);
            //}
        }
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
