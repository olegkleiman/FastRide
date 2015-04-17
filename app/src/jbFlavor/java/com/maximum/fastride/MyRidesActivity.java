package com.maximum.fastride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.adapters.MyRidesDriverAdapter;
import com.maximum.fastride.adapters.MyRidesPassengerAdapter;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.model.User;
import com.maximum.fastride.utils.ConflictResolvingSyncHandler;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyRidesActivity extends ActionBarActivity {

    private static final String LOG_TAG = "FR.MyRides";

    private static MobileServiceClient wamsClient;
    private MobileServiceSyncTable<Ride> mRidesTable;
    private MobileServiceSyncTable<Join> mJoinsTable;
    private Query mPullDriverQuery;
    private Query mPullPassengerQuery;

    MyRidesDriverAdapter mDriverRidesAdapter;
    MyRidesPassengerAdapter mPassengerRidesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        wamsInit();

        ActionBar actionBar = getSupportActionBar();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setText(getResources().getString(R.string.driverTabCaption))
                .setTabListener(new MyRidesTabListener(new FragmentTabDriver(this))));

        actionBar.addTab(actionBar.newTab()
                .setText(getResources().getString(R.string.passengerTabCaption))
                .setTabListener(new MyRidesTabListener(new FragmentTabPassenger(this))));
    }

    private void wamsInit() {
        try {
            wamsClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    this);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userID = sharedPrefs.getString(Globals.USERIDPREF, "");
            MobileServiceUser wamsUser = new MobileServiceUser(userID);

            String token = sharedPrefs.getString(Globals.WAMSTOKENPREF, "");
            // According to this article (http://www.thejoyofcode.com/Setting_the_auth_token_in_the_Mobile_Services_client_and_caching_the_user_rsquo_s_identity_Day_10_.aspx)
            // this should be JWT token, so use WAMS_TOKEN
            wamsUser.setAuthenticationToken(token);

            wamsClient.setCurrentUser(wamsUser);

            User myUser = User.load(this);

            mPullDriverQuery = wamsClient.getTable("rides", Ride.class)
                    .where()
                    .field("driverid")
                    .eq(myUser.getRegistrationId())
                    .and().field("created").le(new Date());

            mPullPassengerQuery = wamsClient.getTable("joins", Join.class)
                    .where();
//                    .field("passenger_id")
//                    .eq(myUser.getRegistrationId())
//                    .and().field("when_joined").le(new Date());
            mPullPassengerQuery.parameter("passenger_id", myUser.getRegistrationId());

            SQLiteLocalStore mLocalStore =
                    new SQLiteLocalStore(wamsClient.getContext(),
                            "myrides", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
            MobileServiceSyncContext syncContext = wamsClient.getSyncContext();
            if (!syncContext.isInitialized()) {
                Map<String, ColumnDataType> ridesTableDefinition = new HashMap<>();
                ridesTableDefinition.put("id", ColumnDataType.String);
                ridesTableDefinition.put("ridecode", ColumnDataType.String);
                ridesTableDefinition.put("driverid", ColumnDataType.String);
                ridesTableDefinition.put("created", ColumnDataType.Date);
                ridesTableDefinition.put("carnumber", ColumnDataType.String);
                ridesTableDefinition.put("approved", ColumnDataType.Boolean);
                ridesTableDefinition.put("__deleted", ColumnDataType.Boolean);
                ridesTableDefinition.put("__version", ColumnDataType.String);
                mLocalStore.defineTable("rides", ridesTableDefinition);

                Map<String, ColumnDataType> joinsTableDefinition = new HashMap<>();
                joinsTableDefinition.put("id", ColumnDataType.String);
                joinsTableDefinition.put("deviceid", ColumnDataType.String);
                joinsTableDefinition.put("passengerid", ColumnDataType.String);
                joinsTableDefinition.put("when_joined", ColumnDataType.Date);
                joinsTableDefinition.put("rideid", ColumnDataType.String);
                joinsTableDefinition.put("ridecode", ColumnDataType.String);
                joinsTableDefinition.put("__deleted", ColumnDataType.Boolean);
                joinsTableDefinition.put("__version", ColumnDataType.String);
                mLocalStore.defineTable("joins", joinsTableDefinition);

                syncContext.initialize(mLocalStore, handler).get();
            }

            mRidesTable = wamsClient.getSyncTable("rides", Ride.class);
            mJoinsTable = wamsClient.getSyncTable("joins", Join.class);

        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }

    }

    private void refreshRides() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    final MobileServiceList<Ride> rides = mRidesTable
                            .read(mPullDriverQuery)
                            .get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( mDriverRidesAdapter == null )
                                return;

                            mDriverRidesAdapter.clear();

                            for (Ride _ride : rides) {
                                Log.i(LOG_TAG, _ride.getCreated().toString());
                                mDriverRidesAdapter.add(_ride);
                            }
                        }
                    });

                } catch(final Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MyRidesActivity.this,
                                    ex.getCause().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }
        }.execute();

    }

    private void refreshJoins() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    final MobileServiceList<Join> joins = mJoinsTable
                            .read(mPullPassengerQuery)
                            .get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( mPassengerRidesAdapter == null )
                                return;

                            mPassengerRidesAdapter.clear();

                            for (Join _join : joins) {
                                Log.i(LOG_TAG, _join.getWhenJoined().toString());
                                mPassengerRidesAdapter.add(_join);
                            }
                        }
                    });
                } catch(final Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MyRidesActivity.this,
                                    ex.getCause().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_rides, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() ) {
            case R.id.action_refresh: {

                ActionBar actionBar = getSupportActionBar();
                ActionBar.Tab selectedTab = actionBar.getSelectedTab();
                int position = selectedTab.getPosition();

                item.setActionView(R.layout.action_progress);

                if( position == 0) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPostExecute(Void result) {
                            // Ensure ProgressBar becomes original 'Refresh' menu item
                            invalidateOptionsMenu();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                mRidesTable.purge(mPullDriverQuery);
                                mRidesTable.pull(mPullDriverQuery).get();

                                refreshRides();
                            } catch (Exception ex) {
                                Log.e(LOG_TAG, ex.getCause().toString());
                            }

                            return null;
                        }
                    }.execute();
                } else if( position == 1 ) { // Passenger tab {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPostExecute(Void result) {
                            // Ensure ProgressBar becomes original 'Refresh' menu item
                            invalidateOptionsMenu();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                mJoinsTable.purge(mPullPassengerQuery);
                                mJoinsTable.pull(mPullPassengerQuery).get();

                                refreshJoins();
                            } catch (Exception ex) {
                                Log.e(LOG_TAG, ex.getCause().toString());
                            }

                            return null;
                        }
                    }.execute();
                }
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabDriver extends android.support.v4.app.Fragment {

        Context context;

        public FragmentTabDriver(Context context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.my_rides_driver, container, false);

            ListView myRidesListView = (ListView)view.findViewById(R.id.listViewMyDriver);
            mDriverRidesAdapter = new MyRidesDriverAdapter(MyRidesActivity.this,
                    R.layout.my_rides_driver_row );
            myRidesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
            myRidesListView.setAdapter(mDriverRidesAdapter);

            refreshRides();

            return view;
        }
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabPassenger extends android.support.v4.app.Fragment{

        Context context;

        public FragmentTabPassenger(Context context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.my_rides_passenger, container, false);

            ListView myJoinsListView = (ListView) view.findViewById(R.id.listViewMyPassenger);
            mPassengerRidesAdapter = new MyRidesPassengerAdapter(MyRidesActivity.this,
                    R.layout.my_rides_passenger_row);
            myJoinsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                }

            });
            myJoinsListView.setAdapter(mPassengerRidesAdapter);

            refreshJoins();

            return view;
        }

    }

    public class MyRidesTabListener implements ActionBar.TabListener {

        Fragment fragment;

        public MyRidesTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }
}
