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
import com.maximum.fastride.views.LayoutRipple;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    public MyRidesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.rides_general_item, parent, false);

        return new ViewHolder(v, mClickListener);

    }

    @Override
    public void onBindViewHolder(MyRidesAdapter.ViewHolder holder, int position) {

        Ride ride = items.get(position);


        //TODO: need to enter the real name of driver
        if(ride.getNameDriver() != "current Driver")
        {
            holder.driverName.setText(ride.getNameDriver());
            holder.ApprovedSing.setVisibility(View.GONE);
            holder.SteeringWheel.setVisibility(View.GONE);

        }
        else {

                holder.driverName.setVisibility(View.GONE);

                if(ride.getApproved() == null){

                }
                else if(ride.getApproved() == true){

                    holder.ApprovedSing.setImageResource(R.drawable.v_sing_26);
                }
                else if ( ride.getApproved() == false){
                    holder.ApprovedSing.setImageResource(R.drawable.ex_sing_26);
                }
        }


        holder.DriverImage.setImageResource(R.drawable.driver50);

        DateFormat df = new SimpleDateFormat("MM.dd.yy");
        holder.created.setText(df.format(ride.getCreated()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView DriverImage;
        ImageView ApprovedSing;
        ImageView SteeringWheel;
        TextView driverName;
        TextView carNumber;
        TextView created;
        //LinearLayout rowLayout;
        LayoutRipple rowLayout;

        IRecyclerClickListener mClickListener;

        public ViewHolder(View itemView,
                          IRecyclerClickListener clickListener) {
            super(itemView);

            mClickListener = clickListener;
            DriverImage = (ImageView) itemView.findViewById(R.id.imageDriver);
            ApprovedSing = (ImageView) itemView.findViewById(R.id.ApprovedSing);
            SteeringWheel = (ImageView) itemView.findViewById(R.id.SteeringWheel);
            driverName = (TextView) itemView.findViewById(R.id.txtDriverName);
            created = (TextView) itemView.findViewById(R.id.txtCreated);
            rowLayout = (LayoutRipple) itemView.findViewById(R.id.myRideRaw);

            rowLayout.setOnClickListener(this);
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
