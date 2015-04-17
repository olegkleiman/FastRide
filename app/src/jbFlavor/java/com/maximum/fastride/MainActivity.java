package com.maximum.fastride;

import android.annotation.SuppressLint;

import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Settings;
import com.google.gson.JsonObject;
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
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

//import org.apache.commons.codec.digest.DigestUtils;


public class MainActivity extends ActionBarActivity { //BaseActivity {

    static final int REGISTER_USER_REQUEST = 1;

	private static final String LOG_TAG = "FR.Main";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private String[] mDrawerTitles;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    public static MobileServiceClient wamsClient;

    ContentQueryMap cqm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Globals.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        // Needed for FB HashKey registration
        String hashKey = Settings.getApplicationSignature(this);

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
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        Log.i(LOG_TAG, "onConfigurationChanged");

//        if( config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.v(LOG_TAG, "Changed to landscape");
//        } else {
//            Log.v(LOG_TAG, "Changed to portrait");
//        }

        setContentView(R.layout.activity_main);
        setupView();

    }

    private void setupView(){
        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerTitles = getResources().getStringArray(R.array.drawers_array);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        User currentUser = User.load(this);
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
//        return true;
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

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {
            switch ( position ){
                case 0: { // Profile
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                break;

                case 1: { // Settings
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                break;

                case 2: { // History
                    Intent intent = new Intent(MainActivity.this, MyRidesActivity.class);
                    startActivity(intent);
                }
                break;

                case 3: { // About
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
                break;
            }

        }
    }

}
