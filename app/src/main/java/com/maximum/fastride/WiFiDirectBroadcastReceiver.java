package com.maximum.fastride;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.util.Log;

import com.maximum.fastride.utils.ITrace;

import java.util.ArrayList;
import java.util.List;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {



    private static final String LOG_TAG = "FR.BR";

    Activity mActivityListener;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       Activity activityListener) {
        super();

        this.mManager = manager;
        this.mChannel = channel;
        this.mActivityListener = activityListener;
    }

    private void _log(String message){
        Log.d(LOG_TAG, message);
        ((ITrace)mActivityListener).trace(LOG_TAG + message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            onStateChanged(intent);
        //      Peers will be obtained from DNS-SD listeners
        //} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
        //    onPeersChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            onConnectionChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            onLocalDeviceChanged(intent);
        }
    }

    private void onStateChanged(Intent intent) {
        String traceMessage;

        // Determine if Wifi P2P mode is enabled or not, alert the Activity.
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            traceMessage = "P2P state changed to enabled";
        } else {
            traceMessage = "P2P state changed to disabled";
            String message = mActivityListener.getString(R.string.enable_wifi_question);
            ((ITrace)mActivityListener).alert(message, Settings.ACTION_WIFI_SETTINGS);
        }

        _log(traceMessage);
    }

    private void onPeersChanged(Intent intent) {
        String traceMessage = "WifiP2p Peers has changed";
        _log(traceMessage);

        // Request available peers from the wifi p2p manager. This is an
        // asynchronous call and the calling activity is notified with a
        // callback on PeerListListener.onPeersAvailable()
        if (mManager != null) {
            mManager.requestPeers(mChannel, (WifiP2pManager.PeerListListener) mActivityListener);
        }
    }

    private void onConnectionChanged(Intent intent){
        String traceMessage = "Connection changed";
        _log(traceMessage);

        WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if(networkInfo.isConnected()) {
            // we are connected with the other device, request connection
            // info to find group owner IP

            traceMessage = "Network connected";
            _log(traceMessage);

            mManager.requestConnectionInfo(mChannel,
                    (WifiP2pManager.ConnectionInfoListener) mActivityListener);

        } else {
            traceMessage = "Network disconnected";
            _log(traceMessage);
        }
    }

    private void onLocalDeviceChanged(Intent intent) {
        String traceMessage = "This device changed";
        _log(traceMessage);

        WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

        traceMessage = "\nDevice name: " + device.deviceName
                + "\nAddress: " + device.deviceAddress
                + "\nType: " + device.primaryDeviceType
                + "\nConnected?" + ((device.status == WifiP2pDevice.CONNECTED) ? "yes" : "no");
        _log(traceMessage);
    }
}