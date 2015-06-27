package com.maximum.fastride.tests;

import android.content.Intent;
import android.net.Uri;

import com.maximum.fastride.utils.WAMSVersionTable;

/**
 * Created by Oleg Kleiman on 20-Jun-15.
 */
public class VersionMismatchListener implements WAMSVersionTable.IVersionMismatchListener {

    @Override
    public void mismatch(int majorLast, int minorLast, String url) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(url));
//        //intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

    }

    @Override
    public void match() {

    }


    @Override
    public void connectionFailure(Exception ex) {

    }
}
