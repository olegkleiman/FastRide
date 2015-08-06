package com.maximum.fastride.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.maximum.fastride.model.Version;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class AutoUpdateService extends Service {

    private static final String LOG_TAG = "FR.autoUpdate";

    public AutoUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPostExecute(Void res){
                 Context ctx = getApplicationContext();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    MobileServiceClient wamsClient =
                            new MobileServiceClient(
                                    Globals.WAMS_URL,
                                    Globals.WAMS_API_KEY,
                                    getApplicationContext());

                    MobileServiceTable<Version> versionTable
                            = wamsClient.getTable("version", Version.class);
                    MobileServiceList<Version> versions =
                            //versionTable.where().execute().get();
                            versionTable.top(1).orderBy("released", QueryOrder.Descending).execute().get();
                    if (versions.getTotalCount() > 0) {
                        Version currentVersion = versions.get(0);
//                wamsVersionMajor = Integer.parseInt(currentVersion.getMajor());
//                wamsVerisonMinor = Integer.parseInt(currentVersion.getMinor());
//                wamsURL = currentVersion.getURL();
                    }

                } catch(MalformedURLException | InterruptedException | ExecutionException ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }


                return null;
            }
        }.execute();


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
