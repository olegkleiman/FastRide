package com.maximum.fastride.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.maximum.fastride.model.Version;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Oleg Kleiman on 20-Jun-15.
 */
public class WAMSVersionTable extends AsyncTask<Void, Void, Void> {

    public interface IVersionMismatchListener{

        public void mismatch(int majorLast, int minorLast, String url);
        public void connectionFailure(Exception ex);
        public void match();

    }

    private static final String LOG_TAG = "FR.WAMSVersion";
    MobileServiceTable<Version> versionTable;
    IVersionMismatchListener mMismatchListener;

    Context mContext;
    int mPackageVersionMajor;
    int mPackageVersionMinor;

    int wamsVersionMajor;
    int wamsVerisonMinor;
    String wamsURL;

    Exception mEx;

    public WAMSVersionTable(Context ctx,
                            IVersionMismatchListener listener) {
        mContext = ctx;
        mMismatchListener = listener;
    }

    public void compare(int major, int minor){

        mPackageVersionMajor = major;
        mPackageVersionMinor = minor;

        this.execute();
    }

    @Override
    protected void onPostExecute(Void result){

        if( mMismatchListener == null )
            return;

        if( mEx != null ) {
            mMismatchListener.connectionFailure(mEx);
            return;
        }

        if( wamsVersionMajor != mPackageVersionMajor
            || wamsVerisonMinor != mPackageVersionMinor) {
               mMismatchListener.mismatch(wamsVersionMajor,
                                            wamsVerisonMinor,
                                            wamsURL);
        } else {
                mMismatchListener.match();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            MobileServiceClient wamsClient =
                    new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    mContext);
            versionTable = wamsClient.getTable("version", Version.class);

            MobileServiceList<Version> versions =
                    //versionTable.where().execute().get();
                    versionTable.top(1).orderBy("released", QueryOrder.Descending).execute().get();
            if (versions.getTotalCount() > 0) {
                Version currentVersion = versions.get(0);
                wamsVersionMajor = Integer.parseInt(currentVersion.getMajor());
                wamsVerisonMinor = Integer.parseInt(currentVersion.getMinor());
                wamsURL = currentVersion.getURL();
            }
        } catch(MalformedURLException | InterruptedException | ExecutionException ex) {
            mEx = ex;
            Log.e(LOG_TAG, ex.getMessage());
        }

        return null;
    }
}
