package com.maximum.fastride.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.maximum.fastride.WiFiDirectBroadcastReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

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

    public void connectToDevice(final WifiP2pConfig config, int delay){

        if( delay == 0) {
            mManager.connect(mChannel, config,
                    new TaggedActionListener("Connected request"));
        } else {

            Handler h = new Handler();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mManager.connect(mChannel, config,
                            new TaggedActionListener("Connected request"));
                }
            };

            h.postDelayed(r, delay); // with delay
        }
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
            String traceMessage = "Client socket created";
            Log.d(LOG_TAG, traceMessage);
            ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

            try {

                // binds this socket to the local address.
                // Because the parameter is null, this socket will  be bound
                // to an available local address and free port
                socket.bind(null);
                InetAddress localAddress = socket.getLocalAddress();

                traceMessage = String.format("Local socket. Address: %s. Port: %d",
                        localAddress.getHostAddress(),
                        socket.getLocalPort());
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

                traceMessage = String.format("Server socket. Address: %s. Port: %d",
                        mGroupHostAddress.getHostAddress(),
                        Globals.SERVER_PORT);
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

                socket.connect(
                        new InetSocketAddress(mGroupHostAddress.getHostAddress(),
                                Globals.SERVER_PORT),
                        Globals.SOCKET_TIMEOUT);

                traceMessage = "Client socket connected";
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

                OutputStream os = socket.getOutputStream();
                os.write(mMessage.getBytes());

                os.close();

                traceMessage = "!!! message written. output closed";
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

            } catch (IOException ex) {
                ex.printStackTrace();
                traceMessage = ex.getMessage();
                Log.e(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);
            } finally {
                try {
                    socket.close();
                    traceMessage = "client socket closed";
                    Log.d(LOG_TAG, traceMessage);
                    ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);
                } catch(Exception e) {
                    traceMessage = e.getMessage();
                    Log.e(LOG_TAG, traceMessage);
                    ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);
                }
            }

            return null;
        }
    }

    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        Context mContext;

        public ServerAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... voids) {

            Log.d(LOG_TAG, "ServerAsyncTask:doBackground() called");

            String traceMessage = "Server: Socket opened on port " + Globals.SERVER_PORT;
            try {
                ServerSocket serverSocket = new ServerSocket(Globals.SERVER_PORT);

                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

                Socket clientSocket = serverSocket.accept();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                traceMessage = reader.readLine();
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

                serverSocket.close();

                traceMessage = "Server socket closed";
                Log.d(LOG_TAG, traceMessage);
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                ((WiFiDirectBroadcastReceiver.IWiFiStateChanges)mContext).trace(traceMessage);
            }

            return null;
        }
    }
}
