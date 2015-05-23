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

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String subTitle = getResources().getString(R.string.subtitle_activity_settings);
        setupUI(subTitle);

        View addButton = findViewById(R.id.add_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addButton.setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    int diameter = getResources().getDimensionPixelSize(R.dimen.diameter);
                    outline.setOval(0, 0, diameter, diameter);
                }
            });
            addButton.setClipToOutline(true);
        }
    }

    public void onButtonAdd(View view){

    }

}
