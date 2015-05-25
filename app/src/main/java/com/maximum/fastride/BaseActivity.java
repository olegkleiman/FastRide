package com.maximum.fastride;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.maximum.fastride.adapters.DrawerRecyclerAdapter;
import com.maximum.fastride.model.User;

/**
 * Created by Oleg Kleiman on 22-May-14.
 */
public class BaseActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final String LOG_TAG = "FR.baseActivity";

    protected String[] mDrawerTitles;
    protected int DRAWER_ICONS[] = {
            R.drawable.ic_action_myrides,
            R.drawable.ic_action_rating,
            R.drawable.ic_action_tutorial,
            R.drawable.ic_action_about};

    RecyclerView mDrawerRecyclerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    protected void setupUI(String subTitle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.fastride_toolbar);
        if( toolbar != null ) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);

            if( !subTitle.isEmpty() ) {
                toolbar.setSubtitle(subTitle);
                TextView txtSubTitle = (TextView) findViewById(R.id.toolbar_subtitle);
                if( txtSubTitle != null )
                    txtSubTitle.setText(subTitle);
            }

        }

        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerRecyclerView.setHasFixedSize(true);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());

        User user = User.load(this);

        mDrawerTitles = getResources().getStringArray(R.array.drawers_array_drawer);
        DrawerRecyclerAdapter drawerRecyclerAdapter =
                new DrawerRecyclerAdapter(this,
                        mDrawerTitles,
                        DRAWER_ICONS,
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getPictureURL());

        mDrawerRecyclerView.setAdapter(drawerRecyclerAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if( mGoogleApiClient != null )
            mGoogleApiClient.disconnect();

        super.onStop();
    }
}
