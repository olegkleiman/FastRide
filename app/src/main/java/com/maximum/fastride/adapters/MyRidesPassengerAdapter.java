package com.maximum.fastride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.maximum.fastride.model.Join;
import com.maximum.fastride.model.Ride;

/**
 * Created by Oleg Kleiman on 17-Apr-15.
 */
public class MyRidesPassengerAdapter extends ArrayAdapter<Join> {

    Context context;
    int layoutResourceId;

    LayoutInflater m_inflater = null;

    public MyRidesPassengerAdapter(Context context,
                                   int layoutResourceId) {
        super(context, layoutResourceId);

        this.context = context;
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
}
