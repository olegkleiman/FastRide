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
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.RideDetailsActivity;
import com.maximum.fastride.adapters.ModesPeersAdapter;
import com.maximum.fastride.adapters.MyRidesAdapter;
import com.maximum.fastride.model.FRMode;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.IRecyclerClickListener;

import java.util.ArrayList;
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
        ride1.setCreated(new Date());
        ride1.setCarNumber("66-777-88");
        rides.add(ride1);

        Ride ride2 = new Ride();
        ride2.setCreated(new Date());
        ride2.setCarNumber("55-888-88");
        rides.add(ride2);

        MyRidesAdapter adapter = new MyRidesAdapter(rides);
        adapter.setOnClickListener(new IRecyclerClickListener() {
                @Override
                public void clicked(View v, int position) {
                    // TODO:
                    Ride currentRide = rides.get(position);
                    Intent intent = new Intent(getActivity(), RideDetailsActivity.class);
//                    intent.putExtra()
                    startActivity(intent);
                }
            });
        recycler.setAdapter(adapter);

        return rootView;

    }



}