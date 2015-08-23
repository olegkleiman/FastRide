package com.maximum.fastride;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.adapters.MyRideTabAdapter;
import com.maximum.fastride.model.GFence;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.wamsUtils;
import com.maximum.fastride.views.SlidingTabLayout;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyRidesActivity extends BaseActivity
        implements ActionBar.TabListener {

    private static final String LOG_TAG = "FR.MyRides";

    MyRideTabAdapter mTabAdapter;
    private String titles[];
    List<Ride> mRides;
    ViewPager mViewPager;
    SlidingTabLayout slidingTabLayout;

    private MobileServiceSyncTable<Ride> mRidesSyncTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        wamsInit(false);
        mRidesSyncTable = getMobileServiceClient().getSyncTable("rides", Ride.class);

        new AsyncTask<Object, Void, Void>() {



            // Runs on UI thread
            @Override
            protected void onPostExecute(Void res) {
                mTabAdapter.updateRides(mRides);
            }

            @Override
            protected Void doInBackground(Object... objects) {

                try {

                    wamsUtils.sync(getMobileServiceClient(), "rides");

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String userID = sharedPrefs.getString(Globals.USERIDPREF, "");

                    Query pullQuery = getMobileServiceClient().getTable(Ride.class)
                            .where().field("driverid").eq(userID);
                    mRidesSyncTable.pull(pullQuery).get();


                    final MobileServiceList<Ride> ridesList = mRidesSyncTable.read(pullQuery).get();

                    mRides = ridesList;


//                   mTabAdapter.notifyDataSetChanged();

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //mRidesAdapter.clear();
//
//                            for(Ride _ride : ridesList) {
//                                Log.d(LOG_TAG, _ride.getRideCode());
//                                //mRidesAdapter.add(_ride);
//                            }
//                        }
//                    });


                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
                }

                return null;
            }
            }.execute();

        setupUI(getString(R.string.subtitle_activity_my_rides), "");

        titles = getResources().getStringArray(R.array.my_rides_titles);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        // Read the rides from local cache
//        new AsyncTask<Object, Void, Void>() {
//
//            @Override
//            protected void onPostExecute(Void result){
//                mViewPager.setAdapter(new MyRideTabAdapter(getSupportFragmentManager(),
//                        titles, mRides));
//            }
//
//            @Override
//            protected Void doInBackground(Object... objects) {
//
//                try {
//                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    String userID = sharedPrefs.getString(Globals.USERIDPREF, "");
//
//                    Query pullQuery = getMobileServiceClient().getTable(Ride.class)
//                            .where().field("driverid").eq(userID);
//                    final MobileServiceList<Ride> ridesList = mRidesSyncTable.read(pullQuery).get();
//                    mRides = ridesList;
//
//                } catch(InterruptedException | ExecutionException ex) {
//                    Log.e(LOG_TAG, ex.getMessage());
//                }
//
//                return null;
//            }
//        }.execute();

//        // TODO: get the rides list from WAMS
          mRides = new ArrayList<Ride>();
        Ride ride1 = new Ride();
        ride1.setNameDriver("current Driver");
        ride1.setCreated(new Date(91, 1, 10));
        ride1.setCarNumber("66-111-88");
        ride1.setApproved(true);
        mRides.add(ride1);

        Ride ride2 = new Ride();
        ride2.setNameDriver("current Driver");
        ride2.setCreated(new Date(82, 1, 10));
        ride2.setCarNumber("66-222-88");
        ride2.setApproved(false);
        mRides.add(ride2);

        Ride ride3 = new Ride();
        ride3.setNameDriver("current Driver");
        ride3.setCreated(new Date(73, 1, 10));
        ride3.setCarNumber("66-333-88");
        //ride3.setApproved(false);
        mRides.add(ride3);

        Ride ride4 = new Ride();
        ride4.setNameDriver("shol");
        ride4.setCreated(new Date(70, 1, 10));
        ride4.setCarNumber("66-444-88");
        ride4.setApproved(true);
        mRides.add(ride4);

        Ride ride5 = new Ride();
        ride5.setNameDriver("current Driver");
        ride5.setCreated(new Date(86, 1, 10));
        ride5.setCarNumber("66-444-88");
        ride5.setApproved(true);
        mRides.add(ride5);

        Ride ride6 = new Ride();
        ride6.setNameDriver("current Driver");
        ride6.setCreated(new Date(70, 9, 10));
        ride6.setCarNumber("66-444-88");
        ride6.setApproved(true);
        mRides.add(ride6);

        Ride ride7 = new Ride();
        ride7.setNameDriver("fisa");
        ride7.setCreated(new Date(95, 1, 16));
        ride7.setCarNumber("66-444-88");
        ride7.setApproved(true);
        mRides.add(ride7);

        Ride ride8 = new Ride();
        ride8.setNameDriver("current Driver");
        ride8.setCreated(new Date(12, 1, 10));
        ride8.setCarNumber("66-444-88");
        ride8.setApproved(false);
        mRides.add(ride8);

        Ride ride9 = new Ride();
        ride9.setNameDriver("current Driver");
        ride9.setCreated(new Date(15, 1, 10));
        ride9.setCarNumber("66-444-88");
        //ride9.setApproved(true);
        mRides.add(ride9);



        mTabAdapter= new MyRideTabAdapter(getSupportFragmentManager(),
                                            titles, mRides);
        mViewPager.setAdapter(mTabAdapter);

        slidingTabLayout.setViewPager(mViewPager);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_rides, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

//    @Override
//    public void clicked(View   view, int position) {
//        Toast.makeText(this, "Yaaaa", Toast.LENGTH_LONG).show();
//
//
//        Intent intent = new Intent(this, DriverRoleActivity.class);
//        //intent.putExtra("aaa",);
//        startActivity(intent);
//
//    }
}
