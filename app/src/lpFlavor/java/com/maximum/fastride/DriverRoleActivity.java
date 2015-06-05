package com.maximum.fastride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maximum.fastride.adapters.WiFiPeersAdapter2;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.FloatingActionButton;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.GroupOwnerSocketHandler;
import com.maximum.fastride.utils.IMessageTarget;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.IRefreshable;
import com.maximum.fastride.utils.ITrace;
import com.maximum.fastride.utils.WiFiUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DriverRoleActivity extends BaseActivity
                implements ITrace,
                           IMessageTarget,
                           Handler.Callback,
                           IRefreshable,
                           IRecyclerClickListener,
                           WiFiUtil.IPeersChangedListener,
                           WifiP2pManager.PeerListListener,
                           WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "FR.Driver";

    Ride mCurrentRide;

    public static MobileServiceClient wamsClient;
    MobileServiceTable<Ride> ridesTable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_role);

        setupUI(getString(R.string.title_activity_driver_role),
                "");
                //getResources().getString(R.string.subtitle_activity_driver_role));
        wamsInit();

        wifiUtil = new WiFiUtil(this);
        wifiUtil.deletePersistentGroups();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserID = sharedPrefs.getString(Globals.USERIDPREF, "");

        // This will publish the service in DNS-SD and start serviceDiscovery()
        wifiUtil.startRegistrationAndDiscovery(this, mUserID);

    }

    protected void setupUI(String title, String subTitle){
        super.setupUI(title, subTitle);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.submit_ride_button);
            fab.setDrawableIcon(getResources().getDrawable(R.drawable.ic_action_done));
            fab.setBackgroundColor(getResources().getColor(R.color.ColorAccent));
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
                        ride.setCarNumber("77-555_99");
                        mCurrentRide = ridesTable.insert(ride).get();

                    } catch(ExecutionException | InterruptedException ex ) {
                        mEx = ex;
                        Log.e(LOG_TAG, ex.getMessage());
                    }

                    return null;
                }
            }.execute();
            //} catch(MalformedURLException | MobileServiceLocalStoreException | ExecutionException | InterruptedException ex ) {
        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }
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
