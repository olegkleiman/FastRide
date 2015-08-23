package com.maximum.fastride.MyRides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maximum.fastride.R;
import com.maximum.fastride.RideDetailsActivity;
import com.maximum.fastride.adapters.MyRidesAdapter;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.IRecyclerClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by eli max on 18/06/2015.
 */
public class RejectedMyRidesFragment extends Fragment {



    List<Ride> mRides;
    private static final String ARG_POSITION = "position";
    private static RejectedMyRidesFragment FragmentInstance;

    MyRidesAdapter adapter;



    public static RejectedMyRidesFragment getInstance() {

        if (FragmentInstance == null ) {
          FragmentInstance = new RejectedMyRidesFragment();
//        Bundle b = new Bundle();
//        b.putInt(ARG_POSITION, position);
//        f.setArguments(b);
        }
        return FragmentInstance;
    }

    public void setRides(List<Ride> rides) {
        mRides = rides;
        if (!mRides.isEmpty()) {
            sort();
            FilteringApproveAndOtherDrivers();
        }
    }

    public void updateRides(List<Ride> rides){

        //!rides.isEmpty()
        if (true) {
            mRides = rides;
            sort();
            FilteringApproveAndOtherDrivers();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_myride_general, container, false);

        RecyclerView recycler = (RecyclerView)rootView.findViewById(R.id.recyclerMyRides);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setItemAnimator(new DefaultItemAnimator());




        adapter = new MyRidesAdapter(mRides);
        adapter.setOnClickListener(new IRecyclerClickListener() {


            @Override
            public void clicked(View v, int position) {
                // TODO:
                Ride currentRide = mRides.get(position);
                Intent intent = new Intent(getActivity(), RideDetailsActivity.class);


                intent.putExtra("ride",  currentRide);
                startActivity(intent);
            }
        });
        recycler.setAdapter(adapter);

        return rootView;

    }

    private  void FilteringApproveAndOtherDrivers(){
        List<Ride> tempList = new ArrayList<Ride>();

        for (Ride ride : mRides ){
            //TODO: the condition: if approve != null need fix
            if(ride.getApproved()!=  null){
                if (ride.getApproved() == false && ride.getNameDriver() == "current Driver") {
                    tempList.add(ride);
                }
            }
        }
        mRides =  tempList;
    }



    private void sort(){

        Collections.sort(mRides, new Comparator<Ride>() {
            public int compare(Ride r1, Ride r2) {
                return r1.getCreated().compareTo(r2.getCreated());
            }
        });
    }
}
