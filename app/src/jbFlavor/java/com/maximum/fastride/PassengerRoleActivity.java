package com.maximum.fastride;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
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
import com.maximum.fastride.utils.Globals;
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

public class PassengerRoleActivity extends ActionBarActivity
        implements WiFiDirectBroadcastReceiver.IWiFiStateChanges,
                   WifiP2pManager.PeerListListener,
                   WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "FR.Passenger";

    public static MobileServiceClient wamsClient;
    MobileServiceTable<Join> joinsTable;

    WiFiUtil wifiUtil;
    public List<WifiP2pDevice> peers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        wamsInit();

        wifiUtil = new WiFiUtil(this);
        wifiUtil.discoverPeers();
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
    public void trace(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //appendStatus(status);
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
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

        TextView txtMe = (TextView)findViewById(R.id.txtMe);
        if (p2pInfo.isGroupOwner) {
            txtMe.setText("ME: GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());
        } else {
            txtMe.setText("ME: NOT GroupOwner, Group Owner IP: " + p2pInfo.groupOwnerAddress.getHostAddress());
        }

        if ( !p2pInfo.isGroupOwner) {

            new ClientAsyncTask(this, p2pInfo.groupOwnerAddress,
                    "From client").execute();
        }
    }

    // Implements WifiP2pManager.PeerListListener

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        String traceMessage = "Peers Available";

        if( wifiP2pDeviceList.getDeviceList().size() == 0 )
            traceMessage = "No peers discovered";

        // Out with the old, in with the new.
        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        //mPeersAdapter.notifyDataSetChanged();
    }

    public static class ClientAsyncTask extends AsyncTask<Void, Void, String> {

        Context mContext;
        String mMessage;
        InetAddress mGroupHostAddress;

        public ClientAsyncTask(Context context,
                               InetAddress groupOwnerAddress,
                               String message){
            this.mContext = context;
            this.mGroupHostAddress = groupOwnerAddress;
            this.mMessage = message;
        }

        @Override
        protected String doInBackground(Void... voids) {

            Log.d(LOG_TAG, "ClientAsyncTask:doBackground() called");

            Socket socket = new Socket();
            try {

                String traceMessage = "Client socket created";
                Log.d(LOG_TAG, traceMessage);

                // binds this socket to the local address.
                // Because the parameter is null, this socket will  be bound
                // to an available local address and free port
                socket.bind(null);
                InetAddress localAddress = socket.getLocalAddress();

                traceMessage = String.format("Local socket. Address: %s. Port: %d",
                        localAddress.getHostAddress(),
                        socket.getLocalPort());
                Log.d(LOG_TAG, traceMessage);

                traceMessage = String.format("Server socket. Address: %s. Port: %d",
                        mGroupHostAddress.getHostAddress(),
                        Globals.SERVER_PORT);
                Log.d(LOG_TAG, traceMessage);

                socket.connect(
                        new InetSocketAddress(mGroupHostAddress.getHostAddress(),
                                Globals.SERVER_PORT),
                        Globals.SOCKET_TIMEOUT);

                traceMessage = "Client socket connected";
                Log.d(LOG_TAG, traceMessage);

                OutputStream os = socket.getOutputStream();
                os.write(mMessage.getBytes());

                os.close();

            } catch (IOException ex) {
                Log.e(LOG_TAG, ex.getMessage());
            } finally {
                try {
                    socket.close();
                } catch(Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            return null;
        }
    }
}
