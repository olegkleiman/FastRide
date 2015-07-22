package com.maximum.fastride.MyRides;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.RideDetailsActivity;
import com.maximum.fastride.adapters.ModesPeersAdapter;
import com.maximum.fastride.adapters.MyRidesAdapter;
import com.maximum.fastride.model.FRMode;
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
public class GeneralMyRidesFragment extends Fragment{


    List<Ride> rides;
    private static final String ARG_POSITION = "position";


    public static GeneralMyRidesFragment newInstance(int position) {
        GeneralMyRidesFragment f = new GeneralMyRidesFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
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

        RecyclerView recycler = (RecyclerView)rootView.findViewById(R.id.recyclerMyRides);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.setItemAnimator(new DefaultItemAnimator());




        // TODO: get the rides list from WAMS
        rides = new ArrayList<Ride>();
        Ride ride1 = new Ride();
        ride1.setNameDriver("current Driver");
        ride1.setCreated(new Date(91, 1, 10));
        ride1.setCarNumber("66-111-88");
        ride1.setApproved(true);
        rides.add(ride1);

        Ride ride2 = new Ride();
        ride2.setNameDriver("current Driver");
        ride2.setCreated(new Date(82, 1, 10));
        ride2.setCarNumber("66-222-88");
        ride2.setApproved(false);
        rides.add(ride2);

        Ride ride3 = new Ride();
        ride3.setNameDriver("current Driver");
        ride3.setCreated(new Date(73, 1, 10));
        ride3.setCarNumber("66-333-88");
        //ride3.setApproved(false);
        rides.add(ride3);

        Ride ride4 = new Ride();
        ride4.setNameDriver("shol");
        ride4.setCreated(new Date(70, 1, 10));
        ride4.setCarNumber("66-444-88");
        ride4.setApproved(true);
        rides.add(ride4);

        Ride ride5 = new Ride();
        ride5.setNameDriver("current Driver");
        ride5.setCreated(new Date(86, 1, 10));
        ride5.setCarNumber("66-444-88");
        ride5.setApproved(true);
        rides.add(ride5);

        Ride ride6 = new Ride();
        ride6.setNameDriver("current Driver");
        ride6.setCreated(new Date(70, 9, 10));
        ride6.setCarNumber("66-444-88");
        ride6.setApproved(true);
        rides.add(ride6);

        Ride ride7 = new Ride();
        ride7.setNameDriver("fisa");
        ride7.setCreated(new Date(95, 1, 16));
        ride7.setCarNumber("66-444-88");
        ride7.setApproved(true);
        rides.add(ride7);

        Ride ride8 = new Ride();
        ride8.setNameDriver("current Driver");
        ride8.setCreated(new Date(12, 1, 10));
        ride8.setCarNumber("66-444-88");
        ride8.setApproved(false);
        rides.add(ride8);

        Ride ride9 = new Ride();
        ride9.setNameDriver("current Driver");
        ride9.setCreated(new Date(15, 1, 10));
        ride9.setCarNumber("66-444-88");
        //ride9.setApproved(true);
        rides.add(ride9);

        sort();

        MyRidesAdapter adapter = new MyRidesAdapter(rides);
        adapter.setOnClickListener(new IRecyclerClickListener() {
                @Override
                public void clicked(View v, int position) {
                    // TODO:
                    Ride currentRide = rides.get(position);
                    Intent intent = new Intent(getActivity(), RideDetailsActivity.class);


                    intent.putExtra("ride",  currentRide);
                    startActivity(intent);
                }
            });
        recycler.setAdapter(adapter);

        return rootView;

    }

    private void sort(){

        Collections.sort(rides,new Comparator<Ride>() {
                public int compare(Ride r1, Ride r2) {
            return r1.getCreated().compareTo(r2.getCreated());
        }
        });
    }



}