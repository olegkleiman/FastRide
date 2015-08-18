package com.maximum.fastride.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.maximum.fastride.R;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * Created by Oleg on 18-Aug-15.
 */
public class wamsBlobUpload extends AsyncTask<File, Void, Void> {

    private static final String LOG_TAG = "FR.wamsUpload";

    URI publishedUri;
    Exception error;

    Context mContext;
    IPictureURLUpdater mUrlUpdater;

    ProgressDialog mProgressDialog;

    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "AccountName=fastride;" +
                    "AccountKey=tuyeJ4EmEuaoeGsvptgyXD0Evvsu1cTiYPAF2cwaDzcGkONdAOZ/3VEY1RHAmGXmXwwkrPN1yQmRVdchXQVgIQ==";

    public wamsBlobUpload(Context ctx){
        mContext = ctx;
        if( ctx instanceof IPictureURLUpdater )
            mUrlUpdater = (IPictureURLUpdater)ctx;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(mContext,
                                    mContext.getString(R.string.detection_store),
                                    mContext.getString(R.string.detection_wait));
    }


    @Override
    protected void onPostExecute(Void result) {
        mProgressDialog.dismiss();

        if( mUrlUpdater != null )
            mUrlUpdater.update(publishedUri.toString());
    }


    @Override
    protected Void doInBackground(File... params) {

        File photoFile = params[0];

        String containerName = "pictures";

        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            String fileName = photoFile.getName();

            CloudBlockBlob blob = container.getBlockBlobReference(fileName);
            blob.upload(new FileInputStream(photoFile), photoFile.length());

            publishedUri = blob.getQualifiedUri();

        } catch (URISyntaxException | InvalidKeyException
                | IOException | StorageException e) {
            error = e;
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }
}
