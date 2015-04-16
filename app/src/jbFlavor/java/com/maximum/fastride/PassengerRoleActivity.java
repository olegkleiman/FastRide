package com.maximum.fastride;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.maximum.fastride.R;
import com.maximum.fastride.model.Join;
import com.maximum.fastride.model.Ride;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class PassengerRoleActivity extends ActionBarActivity {

    private static final String LOG_TAG = "FR.Passenger";

    public static MobileServiceClient wamsClient;
    MobileServiceTable<Join> joinsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        wamsInit();
    }

    private void wamsInit( ) {
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

            joinsTable = wamsClient.getTable("joins", Join.class);

        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }

    }

    public void onSubmit(View view){
        final EditText editRideCode = (EditText)findViewById(R.id.editTextRideCode);
        final String rideCode = editRideCode.getText().toString();
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        new AsyncTask<Void, Void, Void>() {

            Exception mEx;
            String mRideCode;

            @Override
            protected void onPostExecute(Void result){

                if( mEx != null ) {

                    String msg = mEx.getMessage();
                    String[] tokens = msg.split(":");
                    if(tokens.length > 1)
                        msg = tokens[1];

                    if( mEx instanceof ExecutionException) {
                        editRideCode.setError(msg);
                    } else {
                        Toast.makeText(PassengerRoleActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try{
                    Join _join  = new Join();
                    _join.setWhenJoined(new Date());
                    _join.setRideCode(rideCode);
                    _join.setDeviceId(android_id);

                    joinsTable.insert(_join).get();

                } catch(ExecutionException| InterruptedException ex ) {
                    mEx = ex;
                    Log.e(LOG_TAG, ex.getMessage());
                }

                return null;
            }
        }.execute();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_passenger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
