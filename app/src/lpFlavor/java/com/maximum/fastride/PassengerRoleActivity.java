package com.maximum.fastride;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.adapters.WiFiPeersAdapter2;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.utils.ClientSocketHandler;
import com.maximum.fastride.utils.FloatingActionButton;
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
        WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "FR.Passenger";


    MobileServiceTable<Join> joinsTable;

    WiFiUtil wifiUtil;
    WiFiPeersAdapter2 mDriversAdapter;
    public List<WifiP2pDeviceUser> drivers = new ArrayList<>();

    TextView mTxtStatus;
    String mUserID;

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

        final View v = findViewById(R.id.passenger_snackbar);
        final TextView txtStatus = (TextView)findViewById(R.id.status_monitor);
        new Thread() {
            @Override
            public void run(){
                try {
                    while (true) {
                        if (Globals.isInGeofenceArea()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if( v.getVisibility() != View.VISIBLE) {
                                        v.setVisibility(View.VISIBLE);

                                        txtStatus.setText(Globals.getMonitorStatus());
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if( v.getVisibility() != View.GONE)
                                        v.setVisibility(View.GONE);
                                }
                            });

                        }
                        Thread.sleep(1000);
                    }
                } catch(Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }
            }
        }.start();

        // This will start serviceDiscovery
        // for (hopefully) already published service
        wifiUtil.startRegistrationAndDiscovery(this, mUserID);

    }

    protected void setupUI(String title, String subTitle){

        super.setupUI(title, subTitle);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.submit_passenger_button);

            if( fab != null ) {
                fab.setBackgroundColor(getResources().getColor(R.color.ColorAccent));
                Drawable iconDone = getResources().getDrawable(R.drawable.ic_action_done);
                if( iconDone != null ) {
                    fab.setDrawableIcon(iconDone);
                }
            }
        }

        RecyclerView driversRecycler = (RecyclerView)findViewById(R.id.recyclerViewDrivers);
        driversRecycler.setHasFixedSize(true);
        driversRecycler.setLayoutManager(new LinearLayoutManager(this));
        driversRecycler.setItemAnimator(new DefaultItemAnimator());

        mDriversAdapter = new WiFiPeersAdapter2(this, R.layout.drivers_header, drivers);
        driversRecycler.setAdapter(mDriversAdapter);

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

//
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        mDrawerToggle.syncState();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_passenger, menu);
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
        } else if( id == R.id.action_pass_refresh) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.ride_code_layout);
            layout.setVisibility(View.GONE);
            FrameLayout transmitterLayout = (FrameLayout)findViewById(R.id.ride_transmitter_layout);
            transmitterLayout.setVisibility(View.VISIBLE);
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

    private void onSubmit(View view) {
        final EditText editRideCode = (EditText)findViewById(R.id.editTextRideCode);
        final String rideCode = editRideCode.getText().toString();
        onSubmitCode(rideCode);
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
    public void alert(final String strIntent) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if( which == DialogInterface.BUTTON_POSITIVE ) {
                    startActivity(new Intent(strIntent));
                }
            }};

        new AlertDialogWrapper.Builder(this)
                .setTitle(R.string.enable_wifi_question)
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
            }
        });
    }

    //
    // Implementation of IPeerClickListener
    //
    public void clicked(View view, int position) {

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
