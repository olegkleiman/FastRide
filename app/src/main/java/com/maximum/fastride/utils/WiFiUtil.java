package com.maximum.fastride.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.maximum.fastride.WiFiDirectBroadcastReceiver;
import com.maximum.fastride.adapters.WifiP2pDeviceUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.Settings.Global.getString;

/**
 * Created by Oleg Kleiman on 26-Apr-15.
 */
public class WiFiUtil {

    private static final String LOG_TAG = "FR.WiFiUtil";

    private Context mContext;

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pDnsSdServiceRequest mServiceRequest;

    WiFiDirectBroadcastReceiver mReceiver;

    public interface IPeersChangedListener {
        public void add(WifiP2pDeviceUser device);
    }

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

    // Group of methods using Nearby Google API
    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_ETHERNET};
    private boolean isConnectedToNetwork() {
        ConnectivityManager connManager = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return (info != null && info.isConnectedOrConnecting());
    }


    public void startAdvertising(GoogleApiClient googleApiClient,
                                 Connections.ConnectionRequestListener connectionRequestListener) {
        if (!isConnectedToNetwork()) {
            Log.e(LOG_TAG, "startAdvertising: not connected to WiFi network.");
            return;
        }
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(Globals.SERVICE_INSTANCE));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        // Advertise for Nearby Connections. This will broadcast the service id defined in
        // AndroidManifest.xml. By passing 'null' for the name, the Nearby Connections API
        // will construct a default name based on device model such as 'LGE Nexus 5'.
        String serviceName =null;
        Nearby.Connections.startAdvertising(googleApiClient,
                serviceName,
                appMetadata,
                Globals.TIMEOUT_ADVERTISE,
                connectionRequestListener)
        .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
            @Override
            public void onResult(Connections.StartAdvertisingResult result) {
                if (result.getStatus().isSuccess()) {
                    // Device is advertising
                } else {
                    int statusCode = result.getStatus().getStatusCode();
                }
            }
        });
    }

    /**
     * Begin discovering devices advertising Nearby Connections, if possible.
     */
    public void startDiscovery(GoogleApiClient googleApiClient,
                               String serviceId,
                               Connections.EndpointDiscoveryListener endpointDiscoveryListener) {
        if (!isConnectedToNetwork()) {
            Log.e(LOG_TAG, "startDiscovery: not connected to WiFi network.");
            return;
        }


        Nearby.Connections.startDiscovery(googleApiClient,
                                        serviceId,
                                        Globals.TIMEOUT_DISCOVER,
                                        endpointDiscoveryListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status){
                        if (status.isSuccess()) {
                            // Device is discovering
                        } else {

                            int statusCode = status.getStatusCode();
                        }
                    }
                });

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
        mManager.discoverPeers(mChannel,
                new TaggedActionListener((ITrace) mContext, "discover peers request"));
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    public void startRegistrationAndDiscovery(final IPeersChangedListener peersChangedListener,
                                              final String userName) {

        mManager.clearLocalServices(mChannel,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        registerDnsSdService(userName);
                        discoverService(peersChangedListener);
                    }

                    @Override
                    public void onFailure(int errCode) {
                        Log.e(LOG_TAG, "Failed to clear previous registration of local service");
                    }
                });

    }

    public void registerDnsSdService(String userName) {
        Map<String, String> record = new HashMap<>();
        record.put(Globals.TXTRECORD_PROP_AVAILABLE, "visible");
        record.put(Globals.TXTRECORD_PROP_USERNAME, userName);
        record.put(Globals.TXTRECORD_PROP_PORT, Integer.toString(Globals.SERVER_PORT));

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                Globals.SERVICE_INSTANCE,
                Globals.SERVICE_REG_TYPE, record);

        mManager.addLocalService(mChannel, service,
                new TaggedActionListener((ITrace) mContext, "Add Local Service"));
    }

    public void discoverService(final IPeersChangedListener peersChangedListener) {

        final HashMap<String, String> buddies = new HashMap<>();

          /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

        mManager.setDnsSdResponseListeners(mChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType,
                                                        WifiP2pDevice device) {

                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(Globals.SERVICE_INSTANCE)) {
                            String traceMessage = "DNS-SD SRV Record: " + instanceName;
                            ((ITrace) mContext).trace(traceMessage);
                            Log.d(LOG_TAG, traceMessage);

                            if (peersChangedListener != null) {
                                WifiP2pDeviceUser deviceUser =
                                        new WifiP2pDeviceUser(device);
                                String userId = buddies.get(device.deviceName);
                                deviceUser.setUserId(userId);
                                peersChangedListener.add(deviceUser);
                            }
                        } else {
                            Log.d(LOG_TAG, "Other DNS_SD service discovered: "
                                    + instanceName);
                        }
                    }
                },
                new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                     /* Callback includes:
                     * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
                     * record: TXT record dta as a map of key/value pairs.
                     * device: The device running the advertised service.
                     */
                    public void onDnsSdTxtRecordAvailable(String fullDomainName,
                                                          Map<String, String> record,
                                                          WifiP2pDevice device) {

                        String traceMessage = "DNS-SD TXT Records: " +
                                device.deviceName + " is " + record.get(Globals.TXTRECORD_PROP_AVAILABLE);
                        traceMessage += "\nUser Name:" + record.get(Globals.TXTRECORD_PROP_USERNAME);
                        ((ITrace) mContext).trace(traceMessage);
                        Log.d(LOG_TAG, traceMessage);

                        buddies.put(device.deviceName, record.get(Globals.TXTRECORD_PROP_USERNAME));
                    }
                });

        // After attaching listeners, create a new service request and initiate
        // discovery.
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, mServiceRequest,
                new TaggedActionListener((ITrace)mContext, "add service discovery request"));

        mManager.discoverServices(mChannel,
                new TaggedActionListener((ITrace) mContext, "service discovery init"));

        ((ITrace)mContext).trace("discovery started");

    }

    public void removeServiceRequest(){
        if( mServiceRequest != null ) {
            mManager.removeServiceRequest(mChannel, mServiceRequest,
                    new TaggedActionListener((ITrace)mContext, "remove service request"));
        }
    }

    public void removeGroup() {
        mManager.removeGroup(mChannel,
                new TaggedActionListener((ITrace)mContext, "remove group request"));
    }

    public void disconnect(){
        mManager.cancelConnect(mChannel,
                new TaggedActionListener((ITrace) mContext, "cancel connect request"));
    }

    public void connectToDevice(WifiP2pDevice device, int delay){

        removeServiceRequest();

        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        //config.groupOwnerIntent = 15;
        config.wps.setup = WpsInfo.PBC;

        if( delay == 0) {
            ITrace tracer = (ITrace)mContext;
            mManager.connect(mChannel, config,
                    new TaggedActionListener(tracer, "Connected request"));
        } else {

            Handler h = new Handler();

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mManager.connect(mChannel, config,
                            new TaggedActionListener((ITrace)mContext, "Connected request"));
                }
            };

            h.postDelayed(r, delay); // with delay
        }
    }

    public void requestPeers(WifiP2pManager.PeerListListener listener){
        if( mManager != null && mChannel != null) {
            mManager.requestPeers(mChannel, listener);
        }
    }

    public void requestGroupInfo(WifiP2pManager.GroupInfoListener listener) {
        if( mManager != null && mChannel != null )
            mManager.requestGroupInfo(mChannel, listener);
    }

    class TaggedActionListener implements WifiP2pManager.ActionListener{

        String tag;
        ITrace mTracer;

        TaggedActionListener(ITrace tracer, String tag){
            this.tag = tag;
            mTracer = tracer;
        }

        @Override
        public void onSuccess() {
            String message = tag + " succeeded";
            Log.d(LOG_TAG, message);
        }

        @Override
        public void onFailure(int reasonCode) {
            String message = tag + " failed. Reason :" + failureReasonToString(reasonCode);
            if( mTracer != null )
                mTracer.trace(message);
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
            ((ITrace)mContext).trace(traceMessage);

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
                ((ITrace)mContext).trace(traceMessage);

                traceMessage = String.format("Server socket. Address: %s. Port: %d",
                        mGroupHostAddress.getHostAddress(),
                        Globals.SERVER_PORT);
                Log.d(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);

                socket.connect(
                        new InetSocketAddress(mGroupHostAddress.getHostAddress(),
                                              Globals.SERVER_PORT),
                        Globals.SOCKET_TIMEOUT);

                traceMessage = "Client socket connected";
                Log.d(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);

                OutputStream os = socket.getOutputStream();
                os.write(mMessage.getBytes());

                os.close();

                traceMessage = "!!! MESSAGE WRITTEN.\noutput closed";
                Log.d(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);

            } catch (Exception ex) {
                ex.printStackTrace();
                traceMessage = ex.getMessage();
                Log.e(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);
            } finally {
                try {
                    socket.close();
                    traceMessage = "client socket closed";
                    Log.d(LOG_TAG, traceMessage);
                    ((ITrace)mContext).trace(traceMessage);
                } catch(Exception e) {
                    traceMessage = e.getMessage();
                    Log.e(LOG_TAG, traceMessage);
                    ((ITrace)mContext).trace(traceMessage);
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
                ((ITrace)mContext).trace(traceMessage);

                Socket clientSocket = serverSocket.accept();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                traceMessage = reader.readLine();
                Log.d(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);

                clientSocket.close();
                traceMessage = "Client socket closed";
                Log.d(LOG_TAG, traceMessage);

                serverSocket.close();
                traceMessage = "Server socket closed";
                Log.d(LOG_TAG, traceMessage);
                ((ITrace)mContext).trace(traceMessage);

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                ((ITrace)mContext).trace(traceMessage);
            }

            return null;
        }
    }
}
