package com.maximum.fastride;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.GroupOwnerSocketHandler;
import com.maximum.fastride.utils.IMessageTarget;
import com.maximum.fastride.utils.ITrace;
import com.maximum.fastride.utils.WiFiUtil;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PassengerRoleActivity extends BaseActivity // ActionBarActivity
        implements ITrace,
                   IMessageTarget,
                   Handler.Callback,
                   WifiP2pManager.ConnectionInfoListener,
                   WifiP2pManager.GroupInfoListener{

    private static final String LOG_TAG = "FR.Passenger";

    public static MobileServiceClient wamsClient;
    MobileServiceTable<Join> joinsTable;

    WiFiUtil wifiUtil;

    private android.os.Handler handler = new android.os.Handler(this);
    public android.os.Handler getHandler() {
        return handler;
    }

    TextView mTxtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        String subTitle = getResources().getString(R.string.subtitle_activity_passenger_role);
        setupUI(subTitle);

        mTxtStatus = (TextView)findViewById(R.id.txtStatusPassenger);

        wamsInit();

        wifiUtil = new WiFiUtil(this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userID = sharedPrefs.getString(Globals.USERIDPREF, "");

        // This will start serviceDiscovery
        // for (hopefully) already published service
        wifiUtil.startRegistrationAndDiscovery(null, userID);

        // Only register the service that will be discovered from server's side
        //wifiUtil.registerDnsSdService(userID);
    }

    private void wamsInit( ) {
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

            joinsTable = wamsClient.getTable("joins", Join.class);

        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }

    }

    public void onSubmit(View view){
        final EditText editRideCode = (EditText)findViewById(R.id.editTextRideCode);
        final String rideCode = editRideCode.getText().toString();
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


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

                    if( mEx instanceof ExecutionException) {
                        editRideCode.setError(msg);
                    } else {
                        Toast.makeText(PassengerRoleActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_passenger, menu);
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

    //
    // Implementation of WiFiDirectBroadcastReceiver.IWiFiStateChanges
    //


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
    // Implementation of WifiP2pManager.ConnectionInfoListener
    //

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo p2pInfo) {
        TextView txtMe = (TextView)findViewById(R.id.txtPassengerMe);
        Thread handler = null;

        if( !p2pInfo.groupFormed ) {
            trace("Group not formed yet");
            return;
        }

        final Context context = this;


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
//                    new WiFiUtil.ClientAsyncTask(context,
//                            p2pInfo.groupOwnerAddress,
//                            "From client").execute();
//                }
//            };
//
//            h.postDelayed(r, 2000); // let to server to open the socket in advance
        }

        // Optionally may request group info
        //wifiUtil.requestGroupInfo(this) ;
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        String message;

        if (group != null) {
            trace("Group SSID: " + group.getNetworkName());
            trace("Group Size: " + group.getClientList().size());
            trace("Group Network Interface: " + group.getInterface());
            trace("Group Passphrase: " + group.getPassphrase());
            trace("Group Owner: " + group.getOwner().deviceAddress);

            //EnumerateNetworkInterfaces();

            for( WifiP2pDevice client : group.getClientList() ){
                trace("Client: " + client.deviceName);
            }

            WifiP2pDevice p2pDevice = group.getOwner();
            message = "serviceDiscoveryCapable: " +
                    ((p2pDevice.isServiceDiscoveryCapable()) ? true : false);
            trace(message);
            Log.d(LOG_TAG, message);

            message = "wpsDisplaySupported: " +
                    ((p2pDevice.wpsDisplaySupported()) ? true : false);
            trace(message);
            Log.d(LOG_TAG, message);

            message = "wpsKeypadSupported: " +
                    ((p2pDevice.wpsKeypadSupported()) ? true : false);
            trace(message);
            Log.d(LOG_TAG, message);

            message = "wpsPbcSupported: " +
                    ((p2pDevice.wpsPbcSupported()) ? true : false);
            trace(message);
            Log.d(LOG_TAG, message);
        } else {
            message = "Group is NULL";
            trace(message);
            Log.d(LOG_TAG, message);
        }

    }

}
