package com.maximum.fastride;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Settings;
import com.google.gson.JsonObject;
import com.maximum.fastride.adapters.DrawerRecyclerAdapter;
import com.maximum.fastride.gcm.GCMHandler;
import com.maximum.fastride.model.User;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity { //BaseActivity {
//public class MainActivity extends AppCompatActivity {

static final int REGISTER_USER_REQUEST = 1;

	private static final String LOG_TAG = "FR.Main";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mDrawerRecyclerView;
    private String[] mDrawerTitles;
    int DRAWER_ICONS[] = {
            R.drawable.ic_action_myrides,
            R.drawable.ic_action_rating,
            R.drawable.ic_action_logout,
            R.drawable.ic_action_about};

    public static MobileServiceClient wamsClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        if (Globals.DEVELOPER_MODE) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()
//                    .penaltyLog()
//                    .build());
//
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectActivityLeaks()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .build());
//        }

        try {
            // Needed to detect HashCode for FB registration
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
        }
        catch (PackageManager.NameNotFoundException ex) {
            Log.e(LOG_TAG, ex.toString());
        }

        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_main);

        setupView();

        // Intended to be executed only once per app life-time
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if( sharedPrefs.getString(Globals.USERIDPREF, "").isEmpty() ) {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_USER_REQUEST);

        } else {
            NotificationsManager.handleNotifications(this, Globals.SENDER_ID,
                                                    GCMHandler.class);

            String accessToken = sharedPrefs.getString(Globals.TOKENPREF, "");
            wamsInit(accessToken);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(LOG_TAG, "onNewIntent");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Log.i(LOG_TAG, "onConfigurationChanged");

//        if( config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.v(LOG_TAG, "Changed to landscape");
//        } else {
//            Log.v(LOG_TAG, "Changed to portrait");
//        }

        setContentView(R.layout.activity_main);
        setupView();

        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupView(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.fastride_toolbar);
        if( toolbar != null ) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerRecyclerView = (RecyclerView)findViewById(R.id.left_drawer);
        mDrawerRecyclerView.setHasFixedSize(true);
        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDrawerRecyclerView.setItemAnimator(new DefaultItemAnimator());

        User currentUser = User.load(this);

        mDrawerTitles = getResources().getStringArray(R.array.drawers_array_drawer);
        DrawerRecyclerAdapter drawerRecyclerAdapter =
                new DrawerRecyclerAdapter(this,
                        mDrawerTitles,
                        DRAWER_ICONS,
                        currentUser.getFirstName() + " " + currentUser.getLastName(),
                        currentUser.getEmail(),
                        currentUser.getPictureURL());

        mDrawerRecyclerView.setAdapter(drawerRecyclerAdapter);
//        mDrawerRecyclerView.addOnItemClickListener(
//                new RecyclerView.OnItemTouchListener() {
//
//                    @Override
//                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
//                        return false;
//                    }
//
//                    @Override
//                    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
//
//                    }
//                }
//        );

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );
//        {
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerOpened(drawerView);
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerClosed(drawerView);
//            }
//        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerToggle.syncState();

        if( currentUser.isLoaded() ) {
            String pictureURL = currentUser.getPictureURL();

            ImageView imageView = (ImageView) findViewById(R.id.profileImageView);
            Drawable drawable = null;
            try {
                drawable = (Globals.drawMan.userDrawable(this,
                        "1",
                        pictureURL)).get();
                drawable = RoundedDrawable.fromDrawable(drawable);
                ((RoundedDrawable) drawable)
                        .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                        .setBorderColor(Color.LTGRAY)
                        .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                        .setOval(true);

                imageView.setImageDrawable(drawable);
            } catch (InterruptedException | ExecutionException ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");

        NotificationsManager.stopHandlingNotifications(this);
    }

    private void wamsInit(String accessToken){
        try {
            wamsClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    this);

            final JsonObject body = new JsonObject();
            body.addProperty("access_token", accessToken);

            new AsyncTask<Void, Void, Void>() {

                Exception mEx;

                @Override
                protected void onPostExecute(Void result){

                    if( mEx != null ) {
                        Toast.makeText(MainActivity.this,
                                mEx.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected Void doInBackground(Void... voids) {

                    try {
                        MobileServiceUser mobileServiceUser =
                                wamsClient.login(MobileServiceAuthenticationProvider.Facebook,
                                        body).get();
                        saveUser(mobileServiceUser);
                    } catch(ExecutionException | InterruptedException ex ) {
                        mEx = ex;
                        Log.e(LOG_TAG, ex.getMessage());
                    }

                    return null;
                }
            }.execute();
        //} catch(MalformedURLException | MobileServiceLocalStoreException | ExecutionException | InterruptedException ex ) {
        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }
    }

    private void saveUser(MobileServiceUser mobileServiceUser) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Globals.WAMSTOKENPREF, mobileServiceUser.getAuthenticationToken());
        editor.putString(Globals.USERIDPREF, mobileServiceUser.getUserId());
        editor.apply();
    }

    String sha1Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(input, 0, input.length);
        byte[] bytes = digest.digest();

        // This is ~55x faster than looping and String.formating()
        return bytesToHex( bytes );
    }

    String sha1Hash(String toHash) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        byte[] bytes = toHash.getBytes("UTF-8");
        return sha1Hash(bytes);
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//            startActivity(intent);
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        super.onStop();
        Log.d(LOG_TAG, "onStop");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch( requestCode ) {
            case REGISTER_USER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String accessToken = bundle.getString(Globals.TOKENPREF);

                    wamsInit(accessToken);
                    NotificationsManager.handleNotifications(this, Globals.SENDER_ID,
                                                            GCMHandler.class);

                }
            }
            break;

        }
    }

	// Generates random file name 
	@SuppressLint("SimpleDateFormat") 
	private String getTempFileName() {
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "FastRide_" + timeStamp;
	}

    public void onDriverClicked(View v) {
        Intent intent = new Intent(this, DriverRoleActivity.class);
        startActivity(intent);
    }

    public void onPassengerClicked(View v) {
        Intent intent = new Intent(this, PassengerRoleActivity.class);
        startActivity(intent);
    }

}
