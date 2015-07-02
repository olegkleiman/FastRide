package com.maximum.fastride.utils;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Oleg Kleiman on 28-Jun-15.
 */
public class BLEUtil {

    private static final String LOG_TAG = "FR.BLEUtil";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mGattServer;
    private BluetoothGattServerCallback mGattServerCallback;
    FRAdvertiseCallback mAdvCallback;

    Context mContext;

    public interface IDeviceDiscoveredListener {
        void discovered(final BluetoothDevice device);
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if( mContext instanceof IDeviceDiscoveredListener ) {
                    IDeviceDiscoveredListener listener = (IDeviceDiscoveredListener)mContext;
                    listener.discovered(device);
                }
//                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.d(LOG_TAG, "ACTION_DISCOVERY_FINISHED");
            }
        }
    };

    public BLEUtil(Context context) {

        mContext = context;

        // BLE stuff is supported only from API >= 18
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            mAdvCallback = new FRAdvertiseCallback();
            // Register for broadcasts when a device is discovered
            try {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                context.registerReceiver(mReceiver, filter);
            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }
        } else
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScan() {
        if( mBluetoothAdapter != null ) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                }
            }

            boolean bRes = mBluetoothAdapter.startDiscovery();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Boolean startAdvertise() {
        if( !mBluetoothAdapter.isEnabled() ) {
            mBluetoothAdapter.enable();
        }

        BluetoothLeAdvertiser advertiser =
                mBluetoothAdapter.getBluetoothLeAdvertiser();
        //(BluetoothLeAdvertiser)((BluetoothAdapter)((BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter()).getBluetoothLeAdvertiser();
        if( advertiser == null )
            return false;

        startGattServer();

        AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
                                            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                                            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                                            .setConnectable(false)
                                            .setTimeout(10000)
                                            .build();

        List<ParcelUuid> myUUIDs = new ArrayList<ParcelUuid>();
        myUUIDs.add(ParcelUuid.fromString("0000FE00-0000-1000-8000-00805F9B34FB"));
        byte mServiceData[] = { (byte)0xff, (byte)0xfe, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04 };

        ParcelUuid mAdvParcelUUID = ParcelUuid.fromString("0000FEFF-0000-1000-8000-00805F9B34FB");

        AdvertiseData advData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false) //necessity to fit in 31 byte advertisement
                                    .addServiceData(mAdvParcelUUID, mServiceData)
                                    .build();

        advertiser.startAdvertising(advSettings, advData, mAdvCallback);

        return true;
    }

    //Stop BLE advertising and clean up
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertise() {
        BluetoothLeAdvertiser advertiser =
                mBluetoothAdapter.getBluetoothLeAdvertiser();
        advertiser.stopAdvertising(mAdvCallback);
    }

    private void startGattServer() {
        //mGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);

//
//        for(int i = 0; i < advertisingServices.size(); i++) {
//            gattServer.addService(advertisingServices.get(i));
//        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class FRAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect){
            Log.d(LOG_TAG, "onStartSuccess:" + settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            String description = "";
            if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED) description = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
            else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) description = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
            else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) description = "ADVERTISE_FAILED_ALREADY_STARTED";
            else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE) description = "ADVERTISE_FAILED_DATA_TOO_LARGE";
            else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR) description = "ADVERTISE_FAILED_INTERNAL_ERROR";
            else description = "unknown";
            Log.e(LOG_TAG, "onFailure error:" + errorCode + " " + description);
        }

    }
}
