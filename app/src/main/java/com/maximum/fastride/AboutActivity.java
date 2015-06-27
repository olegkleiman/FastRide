package com.maximum.fastride;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.maximum.fastride.utils.WAMSVersionTable;

import java.util.StringTokenizer;

public class AboutActivity extends BaseActivity
                implements WAMSVersionTable.IVersionMismatchListener{

    private static final String LOG_TAG = "FR.About";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String title = getResources().getString(R.string.version_title);

        try{
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            String packageVersionName = info.versionName;
            title = title.concat(" ");
            title = title.concat(packageVersionName);
        }catch(PackageManager.NameNotFoundException ex) {
            Log.e(LOG_TAG, ex.getMessage());
            title = title.concat("<Unknown>");
        } finally {
            setupUI(title, "");
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_check_updates) {
            onCheckUpdate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onCheckUpdate() {
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
    }

    @Override
    public void mismatch(int majorLast, int minorLast, final String url) {
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
    }

    @Override
    public void match() {
        View v = findViewById(R.id.drawer_layout);
        String message = getString(R.string.latest_version);
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void connectionFailure(Exception ex) {

    }
}
