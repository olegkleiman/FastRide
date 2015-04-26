package com.maximum.fastride.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;

import com.maximum.fastride.WiFiDirectBroadcastReceiver;

import java.lang.reflect.Method;

/**
 * Created by Oleg Kleiman on 26-Apr-15.
 */
public class WiFiUtil {

    private static final String LOG_TAG = "FR.WiFiUtil";

    private Context mContext;

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    WiFiDirectBroadcastReceiver mReceiver;

    public WiFiUtil(Context context) {

        mContext = context;

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager)context.getSystemService(context.WIFI_P2P_SERVICE);
        if( mManager != null ) {
            mChannel = mManager.initialize(context, context.getMainLooper(), null);
        }
    }

    public WifiP2pManager getWiFiP2pManager() {
        return mManager;
    }

    public WifiP2pManager.Channel getWiFiP2pChannel() {
        return mChannel;
    }

    public void registerReceiver(Activity listener) {
        /** register the BroadcastReceiver with the intent values to be matched */
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, listener);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    public void unregisterReceiver(){
        mContext.unregisterReceiver(mReceiver);
    }

    public void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }

        }catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new TaggedActionListener("discover peers request"));
    }

    public void connectToDevice(final WifiP2pConfig config){

        Handler h = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                mManager.connect(mChannel, config,
                        new TaggedActionListener("Connected request"));
            }
        };

        h.postDelayed(r, 2000); // 2 second delay
    }

    class TaggedActionListener implements WifiP2pManager.ActionListener{

        String tag;

        TaggedActionListener(String tag){
            this.tag = tag;
        }

        @Override
        public void onSuccess() {
            String message = tag + " succeeded";
            Log.d(LOG_TAG, message);
        }

        @Override
        public void onFailure(int reasonCode) {
            String message = tag + " failed. Reason :" + failureReasonToString(reasonCode);
            Log.d(LOG_TAG, message);
        }

        private String failureReasonToString(int reason) {

            // Failure reason codes:
            // 0 - internal error
            // 1 - P2P unsupported
            // 2- busy

            switch ( reason ){
                case 0:
                    return "Internal Error";

                case 1:
                    return "P2P unsupported";

                case 2:
                    return "Busy";

                default:
                    return "Unknown";
            }
        }
    }
}
