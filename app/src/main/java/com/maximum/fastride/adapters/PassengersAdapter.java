package com.maximum.fastride.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maximum.fastride.R;

import java.util.ArrayList;

/**
 * Created by Oleg Kleiman on 15-Apr-15.
 */
public class PassengersAdapter extends ArrayAdapter<String> {

    private static final String LOG_TAG = "FR.PassengersAdapter";

    Context context;
    Activity activity;
    int layoutResourceId;

    LayoutInflater m_inflater = null;

    public PassengersAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        this.context = context;
        if( context instanceof Activity) {
            this.activity = (Activity)context;
        }
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

//    private ArrayList<DataSetObserver> observers = new ArrayList<DataSetObserver>();

//    @Override
//    public void registerDataSetObserver(DataSetObserver observer) {
//        super.registerDataSetObserver(observer);
//        observers.add(observer);
//
//        Log.i(LOG_TAG, "registerDataSetObserver");
//    }
//
//    @Override
//    public void notifyDataSetChanged(){
//        for(final DataSetObserver observer: observers) {
//            if( this.activity != null ) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        observer.onChanged();
//                    }
//                });
//
//            } else {
//                observer.onChanged();
//            }
//        }
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PassengersHolder holder = null;

        final String passenger = this.getItem(position);

        if( row == null ) {
            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new PassengersHolder();

            holder.txtView = (TextView)row.findViewById(R.id.txtPassengerName);

            row.setTag(holder);
        } else {
            holder = (PassengersHolder)row.getTag();
        }

        holder.txtView.setText(passenger);

        return row;
    }

    static class PassengersHolder {
        TextView txtView;
    }
}
