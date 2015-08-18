package com.maximum.fastride.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.maximum.fastride.R;
import com.maximum.fastride.model.Ride;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Oleg on 18-Aug-15.
 */
public class wamsPictureURLUpdater extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = "FR.wamsUrlUpdater";

    Context mContext;
    IPictureURLUpdater mUrlUpdater;

    MobileServiceClient wamsClient;
    MobileServiceTable<Ride> mRidesTable;
    Exception ex;

    ProgressDialog mProgressDialog;

    public wamsPictureURLUpdater(Context ctx) {
        mContext = ctx;

        if( ctx instanceof IPictureURLUpdater )
            mUrlUpdater = (IPictureURLUpdater)ctx;
    }

    @Override
    protected void onPreExecute() {

        mProgressDialog = ProgressDialog.show(mContext,
                mContext.getString(R.string.detection_update),
                mContext.getString(R.string.detection_wait));


        try {
            wamsClient = wamsUtils.init(mContext);

            mRidesTable = wamsClient.getTable("rides", Ride.class);

        } catch (MalformedURLException e) {
            ex = e;
        }
    }

    @Override
    protected void onPostExecute(Void result) {

        mProgressDialog.dismiss();

        if( mUrlUpdater != null )
            mUrlUpdater.finished();
    }

    @Override
    protected Void doInBackground(String... params) {

        String pictureURL = params[0];
        String rideCode = params[1];

        try {
            MobileServiceList<Ride> rides
                    = mRidesTable.where()
                                .field("ridecode").eq(rideCode)
                                .execute().get();
            if( rides.size() > 0) {
                Ride currentRide = rides.get(0);
                currentRide.setPictureURL(pictureURL);

                mRidesTable.update(currentRide).get();
            }

        } catch (InterruptedException | ExecutionException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }
}
