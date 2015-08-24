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
import java.util.List;


/**
 * Created by eli max on 18/06/2015.
 */
public class GeneralMyRidesFragment extends Fragment{

    List<Ride> mRides = new ArrayList<>();
    private static final String ARG_POSITION = "position";
    private static GeneralMyRidesFragment FragmentInstance;

    RecyclerView   mRecycler;
    MyRidesAdapter mRidesAdapter;

    public static GeneralMyRidesFragment getInstance() {

        if (FragmentInstance == null ) {
            FragmentInstance = new GeneralMyRidesFragment();
        }
        return FragmentInstance;
    }

    public void setRides(List<Ride> rides) {

        if( rides == null )
            return;

        mRides.clear();
        mRides.addAll(rides);

        if (!mRides.isEmpty()) {
            sort();
        }
    }

    public void updateRides(List<Ride> rides){

        if( rides == null )
            return;

        mRides.clear();
        mRides.addAll(rides);

        if (!mRides.isEmpty()) {
            sort();
            mRidesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_myride_general, container, false);

        mRecycler = (RecyclerView)rootView.findViewById(R.id.recyclerMyRides);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setItemAnimator(new DefaultItemAnimator());

        mRidesAdapter = new MyRidesAdapter(mRides);
        mRidesAdapter.setOnClickListener(new IRecyclerClickListener() {
            @Override
            public void clicked(View v, int position) {
                // TODO:
                Ride currentRide = mRides.get(position);
                Intent intent = new Intent(getActivity(), RideDetailsActivity.class);

                intent.putExtra("ride", currentRide);
                startActivity(intent);
            }
        });
        mRecycler.setAdapter(mRidesAdapter);

        return rootView;
    }

    private void sort(){

        Collections.sort(mRides,new Comparator<Ride>() {
                public int compare(Ride r1, Ride r2) {
            return r1.getCreated().compareTo(r2.getCreated());
        }
        });
    }



}