package com.maximum.fastride.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maximum.fastride.R;
import com.maximum.fastride.model.FRMode;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.IRecyclerClickListener;

import java.util.List;

/**
 * Created by eli max on 22/06/2015.
 */
public class MyRidesAdapter extends RecyclerView.Adapter<MyRidesAdapter.ViewHolder> {

    private List<Ride> items;
    IRecyclerClickListener mClickListener;


    public MyRidesAdapter(List<Ride> objects) {
        items = objects;
    }

    public void setOnClickListener(IRecyclerClickListener listener) {
        mClickListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.rides_general_item, parent, false);

        return new ViewHolder(v, mClickListener);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Ride ride = items.get(position);

        holder.DriverImage.setImageResource(R.drawable.driver64);
        holder.carNumber.setText(ride.getCarNumber());
        holder.created.setText(ride.getCreated().toString());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView DriverImage;
        TextView carNumber;
        TextView created;
        LinearLayout rawLayout;

        IRecyclerClickListener mClickListener;

        public ViewHolder(View itemView,
                          IRecyclerClickListener clickListener) {
            super(itemView);

            mClickListener = clickListener;
            DriverImage = (ImageView) itemView.findViewById(R.id.imageDriver);
            carNumber = (TextView) itemView.findViewById(R.id.txtCarNumber);
            created = (TextView) itemView.findViewById(R.id.txtCreated);
            rawLayout = (LinearLayout) itemView.findViewById(R.id.myRideRaw);

            rawLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            v.invalidate();
            int position = this.getLayoutPosition();
            if (mClickListener != null) {
                mClickListener.clicked(v, position);
            }
        }
    }
}
