package com.maximum.fastride.adapters;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maximum.fastride.R;

import java.util.List;

/**
 * Created by Oleg Kleiman on 26-Apr-15.
 */
public class WiFiPeersAdapter extends ArrayAdapter<WifiP2pDevice> {


    private List<WifiP2pDevice> items;
    Context mContext;

    LayoutInflater m_inflater = null;

    public WiFiPeersAdapter(Context context, int textViewResourceId,
                            List<WifiP2pDevice> objects){
        super(context, textViewResourceId, objects);

        mContext = context;
        items = objects;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DeviceHolder holder = null;

        if( row == null ) {

            holder = new DeviceHolder();

            row = m_inflater.inflate(R.layout.row_devices, null);

            holder.deviceName = (TextView)row.findViewById(R.id.device_name);
            holder.deviceDetails = (TextView)row.findViewById(R.id.device_details);
            holder.deviceType = (TextView)row.findViewById(R.id.device_type);
            holder.deviceStatus = (TextView)row.findViewById(R.id.device_status);

            row.setTag(holder);

        } else {
            holder = (DeviceHolder)row.getTag();
        }

        WifiP2pDevice device = items.get(position);
        holder.deviceName.setText(device.deviceName);
        holder.deviceDetails.setText(device.deviceAddress);
        holder.deviceType.setText(device.primaryDeviceType);
        holder.deviceStatus.setText(getDeviceStatus(device.status));

        return row;
    }

    private String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    static class DeviceHolder{
        TextView deviceName;
        TextView deviceDetails;
        TextView deviceType;
        TextView deviceStatus;
    }
}
