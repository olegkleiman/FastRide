package com.maximum.fastride;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.maximum.fastride.R;
import com.maximum.fastride.adapters.PassengersAdapter;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.model.User;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DriverRoleActivity extends ActionBarActivity
    implements WiFiDirectBroadcastReceiver.IWiFiStateChanges {

    private static final String LOG_TAG = "FR.Driver";

    // Wi-Fi Private - related members
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver receiver;
    WifiP2pManager mManager;

    @Override
    public void setIsWifiP2pEnabled(boolean state) {

    }

    public static MobileServiceClient wamsClient;
    MobileServiceTable<Ride> ridesTable;

    public static PassengersAdapter mPassengersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_role);

        ListView listView = (ListView)findViewById(R.id.listView);

        mPassengersAdapter = new PassengersAdapter(DriverRoleActivity.this,
                R.layout.passener_item_row);
        listView.setAdapter(mPassengersAdapter);

        wamsInit();

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(this.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener(){

            @Override
            public void onChannelDisconnected() {
                Log.d(LOG_TAG, "Channel disconnected");
            }
        });
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            @Override
            public void onFailure(int i) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
            }
        });

        Map record = new HashMap();
        record.put("listenport", String.valueOf(8888));
        record.put("buddyname", "Oleg Kleiman");
        record.put("available", "visible");
        WifiP2pServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test",
                "_presence._tcp", record);
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });


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
                String mRideCode;

                @Override
                protected void onPostExecute(Void result){

                    if( mEx == null ) {
                        TextView txtRideCode = (TextView) findViewById(R.id.txtRideCode);
                        txtRideCode.setText(mRideCode);
                    } else {
                        Toast.makeText(DriverRoleActivity.this,
                                mEx.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected Void doInBackground(Void... voids) {

                    try {
                        Ride ride = new Ride();
                        ride = ridesTable.insert(ride).get();

                        mRideCode = ride.getRideCode();
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

    }

    public void onReceive(Context context, Intent intent) {

    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver_role, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
