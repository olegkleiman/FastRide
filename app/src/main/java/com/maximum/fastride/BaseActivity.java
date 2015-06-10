package com.maximum.fastride;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.maximum.fastride.adapters.DrawerAccountAdapter;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.model.User;
import com.maximum.fastride.services.GeofenceErrorMessages;
import com.maximum.fastride.services.GeofenceTransitionsIntentService;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Oleg Kleiman on 22-May-14.
 */
public class BaseActivity extends ActionBarActivity
        implements //GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient; }

    private MobileServiceClient wamsClient;
    public MobileServiceClient getMobileServiceClient() { return wamsClient; }

    private static final String LOG_TAG = "FR.baseActivity";

    protected String[] mDrawerTitles;
    protected int DRAWER_ICONS[] = {
            R.drawable.ic_action_start,
            R.drawable.ic_action_myrides,
            R.drawable.ic_action_rating,
            R.drawable.ic_action_tutorial
    };

    RecyclerView mDrawerRecyclerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {

            GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
                    (GoogleApiClient.OnConnectionFailedListener)this;

            GoogleApiClient.Builder builder =
                    new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API);

            if( this instanceof GoogleApiClient.ConnectionCallbacks) {
                GoogleApiClient.ConnectionCallbacks callbacksImplementer =
                        (GoogleApiClient.ConnectionCallbacks)this;
                builder.addConnectionCallbacks(callbacksImplementer);
            }
            if( connectionFailedListener != null ) {
                builder.addOnConnectionFailedListener(connectionFailedListener);
            }

            mGoogleApiClient = builder.build();

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    protected void setupUI(String title, String subTitle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.fastride_toolbar);
        if( toolbar != null ) {
            setSupportActionBar(toolbar);
            //toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);

            // enable ActionBar app icon to behave as action to toggle nav drawer
            ActionBar actionBar = getSupportActionBar();
            if( actionBar != null ) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);

                actionBar.setTitle(title);
            }

//            TextView txtTitle = (TextView) findViewById(R.id.toolbar_title);
//            txtTitle.setText(title);
//
//            TextView txtSubTitle = (TextView) findViewById(R.id.toolbar_subtitle);
//            if( txtSubTitle != null ) {
//
//                if (subTitle.isEmpty()) {
//                    txtSubTitle.setVisibility(View.GONE);
//                } else {
//                    txtSubTitle.setVisibility(View.VISIBLE);
//                    txtSubTitle.setText(subTitle);
//                    //toolbar.setSubtitle(subTitle);
//                }
//            }
        }

        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerRecyclerView.setHasFixedSize(true);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());

        User user = User.load(this);

        mDrawerTitles = getResources().getStringArray(R.array.drawers_array_drawer);
        DrawerAccountAdapter drawerRecyclerAdapter =
                new DrawerAccountAdapter(this,
                        mDrawerTitles,
                        DRAWER_ICONS,
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getPictureURL());
        mDrawerRecyclerView.setAdapter(drawerRecyclerAdapter);

        final Context ctx = this;

        LinearLayout aboutLayout = (LinearLayout) findViewById(R.id.about_row);
        aboutLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, AboutActivity.class);
                ctx.startActivity(intent);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the Google API client.
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the Google API client invalidates it.
        if( mGoogleApiClient != null )
            mGoogleApiClient.disconnect();

        super.onStop();
    }

    public void wamsInit() {
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

        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }

    }
}
