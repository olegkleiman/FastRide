package com.maximum.fastride;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rekognition.RekoSDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

//import org.apache.commons.codec.digest.DigestUtils;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.azure.storage.*;


public class MainActivity extends BaseActivity {
    //extends Activity { //

    static final int REGISTER_USER_REQUEST = 1;

	private static final String LOG_TAG = "fast_ride";

    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = sharedPrefs.getString("username", "");

        //String myHexHash = DigestUtils.shaHex(myFancyInput);
        String hashUserName = sha1Hash(username);

        if( username.isEmpty() ) {

            try {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_USER_REQUEST);

            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

//        try {
//            mClient = new MobileServiceClient("https://fastride.azure-mobile.net/",
//                    "omCudOMCUJgIGbOklMKYckSiGKajJU91",
//                    this);
//        } catch (MalformedURLException ex) {
//            Log.e(LOG_TAG, ex.getMessage());
//        }


    }

    String sha1Hash(String toHash) {

        String hash = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return hash;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_USER_REQUEST && resultCode == RESULT_OK) {

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
        Intent intent = new Intent(this, ConfirmRideActivity.class);
        startActivity(intent);
    }



}
