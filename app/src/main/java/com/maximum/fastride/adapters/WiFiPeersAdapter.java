package com.maximum.fastride.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maximum.fastride.R;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.RoundedDrawable;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Oleg Kleiman on 26-Apr-15.
 */
public class WiFiPeersAdapter extends ArrayAdapter<WifiP2pDeviceUser> {

    private static final String LOG_TAG = "FR.PeersAdapter";

    private List<WifiP2pDeviceUser> items;
    Context mContext;

    LayoutInflater m_inflater = null;

    public WiFiPeersAdapter(Context context, int textViewResourceId,
                            List<WifiP2pDeviceUser> objects){
        super(context, textViewResourceId, objects);

        mContext = context;
        items = objects;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void updateItem(WifiP2pDeviceUser device){
        int index = items.indexOf(device);
        if( index != -1 )
            items.set(index, device);
    }

    @Override
    public void add(WifiP2pDeviceUser device){
        if( !items.contains(device) )
            items.add(device);
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
            holder.deviceStatus = (TextView)row.findViewById(R.id.device_status);
            holder.userPicture = (ImageView)row.findViewById(R.id.userPicture);

            row.setTag(holder);

        } else {
            holder = (DeviceHolder)row.getTag();
        }

        WifiP2pDeviceUser device = items.get(position);
        holder.deviceName.setText(device.deviceName);
        holder.deviceDetails.setText(device.deviceAddress);
        holder.deviceStatus.setText(getDeviceStatus(device.status));
        String userId = device.getUserId();

        String pictureURL = getUserPictureURL(userId);

        Drawable drawable = null;
        try {
            drawable = (Globals.drawMan.userDrawable(mContext,
                    userId,
                    pictureURL)).get();
            drawable = RoundedDrawable.fromDrawable(drawable);
            ((RoundedDrawable) drawable)
                    .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                    .setBorderColor(Color.LTGRAY)
                    .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                    .setOval(true);

            holder.userPicture.setImageDrawable(drawable);
        } catch (InterruptedException | ExecutionException ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        return row;
    }

    private String getUserPictureURL(String userId){
        String[] tokens = userId.split(":");
        if( tokens.length > 1 ){
            if( Globals.FB_PROVIDER.equals(tokens[0]) ) {
                return "http://graph.facebook.com/" + tokens[1] + "/picture?type=large";
            } else {
                return "";
            }
        } else
            return "";
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
        TextView deviceStatus;
        ImageView userPicture;
    }
}
