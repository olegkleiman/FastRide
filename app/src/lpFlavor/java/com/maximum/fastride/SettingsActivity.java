package com.maximum.fastride;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Outline;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.maximum.fastride.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        View addButton = findViewById(R.id.add_button);
        addButton.setOutlineProvider(new ViewOutlineProvider() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void getOutline(View view, Outline outline) {
                int diameter = getResources().getDimensionPixelSize(R.dimen.diameter);
                outline.setOval(0, 0, diameter, diameter);
            }
        });
        addButton.setClipToOutline(true);
    }

}
