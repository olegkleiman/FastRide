package com.maximum.fastride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.maximum.fastride.model.Ride;

/**
 * Created by Oleg Kleiman on 17-Apr-15.
 */
public class MyRidesDriverAdapter extends ArrayAdapter<Ride> {

    Context context;
    int layoutResourceId;

    LayoutInflater m_inflater = null;

    public MyRidesDriverAdapter(Context context,
                                int layoutResourceId) {
        super(context, layoutResourceId);

        this.context = context;
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
}
