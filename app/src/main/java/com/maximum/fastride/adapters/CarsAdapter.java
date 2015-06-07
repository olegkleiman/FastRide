package com.maximum.fastride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maximum.fastride.R;

import java.util.List;

/**
 * Created by Oleg on 05-Jun-15.
 */
public class CarsAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    List<String> mItems;

    LayoutInflater mInflater;

    public CarsAdapter(Context context,
                       int viewResourceId,
                       List<String> objects){
        super(context, viewResourceId, objects);

        this.context = context;
        this.layoutResourceId = viewResourceId;
        mItems = objects;

        mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public String getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CarHolder holder = null;

        String carNumber = this.getItem(position);

        if( row == null ) {
            row = mInflater.inflate(layoutResourceId, parent, false);
            holder = new CarHolder();
            
            holder.txtView = (TextView)row.findViewById(R.id.txtCarNumber);
            row.setTag(holder);

        } else {
            holder = (CarHolder)row.getTag();
        }

        holder.txtView.setText(carNumber);

        return row;
    }

    static class CarHolder {
        TextView txtView;
    }
}
