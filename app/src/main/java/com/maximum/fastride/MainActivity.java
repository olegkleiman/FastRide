package com.maximum.fastride;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonObject;
import com.maximum.fastride.adapters.ModesPeersAdapter;
import com.maximum.fastride.gcm.GCMHandler;
import com.maximum.fastride.jobs.GeofencesDownloadService;
import com.maximum.fastride.model.FRMode;
import com.maximum.fastride.model.User;
import com.maximum.fastride.services.AutoUpdateService;
import com.maximum.fastride.utils.Globals;
import com.maximum.fastride.utils.IRecyclerClickListener;
import com.maximum.fastride.utils.RoundedDrawable;
import com.maximum.fastride.utils.WAMSVersionTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity
                        implements WAMSVersionTable.IVersionMismatchListener,
                                   IRecyclerClickListener{
static final int REGISTER_USER_REQUEST = 1;

	private static final String LOG_TAG = "FR.Main";

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
        setContentView(R.layout.activity_main);

        // Intended to be executed only once per app life-time
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if( sharedPrefs.getString(Globals.USERIDPREF, "").isEmpty() ) {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_USER_REQUEST);

            // To be continued on onActivityResult()

        } else {
            NotificationsManager.handleNotifications(this, Globals.SENDER_ID,
                    GCMHandler.class);

            String accessToken = sharedPrefs.getString(Globals.TOKENPREF, "");

            // Don't mess with BaseActivity.wamsInit();
            wamsInit(accessToken);

//            Intent autoUpdate = new Intent(this, AutoUpdateService.class);
//            startService(autoUpdate);

            //Application.ActivityLifecycleCallbacks.

            WAMSVersionTable wamsVersionTable = new WAMSVersionTable(this, this);
            try {

                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                String packageVersionName = info.versionName;
                if (!packageVersionName.isEmpty()) {

                    StringTokenizer tokens = new StringTokenizer(packageVersionName, ".");
                    if( tokens.countTokens() > 0 ) {
                        int majorPackageVersion = Integer.parseInt(tokens.nextToken());
                        int minorPackageVersion = Integer.parseInt(tokens.nextToken());

                        wamsVersionTable.compare(majorPackageVersion, minorPackageVersion);
                    }
                }

            }catch(PackageManager.NameNotFoundException ex) {

                Log.e(LOG_TAG, ex.getMessage());
            }

            setupUI(getString(R.string.title_activity_main), "");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobScheduler jobScheduler =
                        (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                ComponentName mServiceComponent;
                mServiceComponent = new ComponentName(this, GeofencesDownloadService.class);
                int jobId = 1;
                JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent)
                        .setOverrideDeadline(Long.valueOf(10) * 1000)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

                int result = jobScheduler.schedule(builder.build());
                if (result == JobScheduler.RESULT_SUCCESS)
                    Log.d(LOG_TAG, "Geofences Update Job scheduled successfully!");
            } else {

                final long REPEAT_TIME = 1000 * 30;

                Calendar cal = Calendar.getInstance();

                AlarmManager alarm =
                        (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, AutoUpdateService.class);
                PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

//              InexactRepeating allows Android to optimize the energy consumption
//                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                                          cal.getTimeInMillis(),
//                                          REPEAT_TIME, pintent);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);
            }
        }
    }

    //
    // Implementation of IVersionMismatchListener
    //
    public void mismatch(int major, int minor, final String url){
        try {

            new MaterialDialog.Builder(this)
                    .title(getString(R.string.new_version_title))
                    .content(getString(R.string.new_version_conent))
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            //intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .show();
        } catch (MaterialDialog.DialogException e) {
            // better that catch the exception here would be use handle to send events the activity
        }
    }

    public void match() {

    }

    public void connectionFailure(Exception ex) {

        if( ex != null ) {

            View v = findViewById(R.id.drawer_layout);
            Snackbar.make(v, ex.getMessage(), Snackbar.LENGTH_LONG);
        }

    }

    protected void setupUI(String title, String subTitle) {

        super.setupUI(title, subTitle);

        try {
            User user = User.load(this);

            ImageView imageAvatar = (ImageView) findViewById(R.id.userAvatarView);

            Drawable drawable =
                    (Globals.drawMan.userDrawable(this,
                    "1",
                    user.getPictureURL())).get();
            if( drawable != null ) {
                drawable = RoundedDrawable.fromDrawable(drawable);
                ((RoundedDrawable) drawable)
                        .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                        .setOval(true);

                imageAvatar.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        RecyclerView recycler = (RecyclerView)findViewById(R.id.recyclerViewModes);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setItemAnimator(new DefaultItemAnimator());

        List<FRMode> modes = new ArrayList<>();
        FRMode mode1 = new FRMode();
        mode1.setName( getString(R.string.mode_name_driver));
        mode1.setImageId(R.drawable.driver64);
        modes.add(mode1);
        FRMode mode2 = new FRMode();
        mode2.setName( getString(R.string.mode_name_passenger) );
        mode2.setImageId(R.drawable.passenger64);
        modes.add(mode2);

        ModesPeersAdapter adapter = new ModesPeersAdapter(this, modes);
        recycler.setAdapter(adapter);

    }

    //
    // Implementation of IRecyclerClickListener
    //

    @Override
    public void clicked(View view, int position){
        switch( position ) {
            case 1:
                onDriverClicked(view);
                break;

            case 2:
                onPassengerClicked(view);
                break;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(LOG_TAG, "onNewIntent");
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
        setupUI(getString(R.string.title_activity_main), "");

        //mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");

        NotificationsManager.stopHandlingNotifications(this);
    }

    public void wamsInit(String accessToken){
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
                    setupUI(getString(R.string.title_activity_main), "");

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
        try {
            Intent intent = new Intent(this, PassengerRoleActivity.class);
            startActivity(intent);
        } catch(Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
