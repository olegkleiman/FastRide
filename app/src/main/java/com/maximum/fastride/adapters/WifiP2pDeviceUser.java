package com.maximum.fastride.adapters;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Oleg Kleiman on 12-May-15.
 */
public class WifiP2pDeviceUser extends WifiP2pDevice {
    private String _userName;
    public String getUserId() {
        return _userName;
    }
    public void setUserId(String value) {
        _userName = value;
    }

    public WifiP2pDeviceUser(WifiP2pDevice device) {
        super(device);
    }

    public WifiP2pDeviceUser(String deviceName,
                             String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

}
