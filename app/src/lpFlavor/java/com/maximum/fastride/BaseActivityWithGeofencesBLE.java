package com.maximum.fastride;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * Created by Oleg Kleiman on 16-Jun-15.
 */
@TargetApi(21)
public class BaseActivityWithGeofencesBLE extends BaseActivityWithGeofences{

    private static final String LOG_TAG = "FR.BLE";
    private static final int ENABLE_BLUETOOTH_REQUEST = 17;
    private static final ParcelUuid URI_BEACON_UUID = ParcelUuid.fromString("0000FED8-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBLE();
        advertiseUriBeacon();
    }

    private void setupBLE() {
        BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

//        if (mBluetoothAdapter == null) {
//
//        } else if (!mBluetoothAdapter.isEnabled()) {
//
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            this.startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);
//
//        }
    }

    private void advertiseUriBeacon() {

        if( mBluetoothAdapter == null )
            return;

        BluetoothLeAdvertiser bleAdvertizer = mBluetoothAdapter.getBluetoothLeAdvertiser();
        AdvertiseSettings bleSettings = getAdvertiseSettings();
        AdvertiseData advertisementData = getAdvertisementData();
        bleAdvertizer.startAdvertising(bleSettings,
                advertisementData,
                advertiseCallback);

    }

    private AdvertiseSettings getAdvertiseSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setConnectable(false);

        return builder.build();
    }

    private AdvertiseData getAdvertisementData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeTxPowerLevel(false); // reserve advertising space for URI

        byte[] beaconData = new byte[7];
        beaconData[0] = 0x00; // flags
        beaconData[1] = (byte) 0xBA; // transmit power
        beaconData[2] = 0x00; // http://www.
        beaconData[3] = 0x65; // e
        beaconData[4] = 0x66; // f
        beaconData[5] = 0x66; // f
        beaconData[6] = 0x08; // .org

        builder.addServiceData(URI_BEACON_UUID, beaconData);

        // Adding 0xFED8 to the "Service Complete List UUID 16" (0x3) for iOS compatibility
        builder.addServiceUuid(URI_BEACON_UUID);

        return builder.build();
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @SuppressLint("Override")
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            final String message = "Advertisement successful";
            Log.d(LOG_TAG, message);

        }

        @SuppressLint("Override")
        @Override
        public void onStartFailure(int i) {
            final String message = "Advertisement failed error code: " + i;
            Log.e(LOG_TAG, message);

        }

    };

}
