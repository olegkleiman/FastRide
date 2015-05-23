package com.maximum.fastride;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.maximum.fastride.adapters.TutorialTabsAdapter;
import com.maximum.fastride.views.SlidingTabLayout;

public class TutorialActivity extends BaseActivity
        implements ActionBar.TabListener{

    TutorialTabsAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;
    SlidingTabLayout slidingTabLayout;
    private String titles[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        setupUI(getResources().getString(R.string.subtitle_activity_tutorial));

        titles = getResources().getStringArray(R.array.tutorial_titles);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mViewPager.setAdapter(new TutorialTabsAdapter(getSupportFragmentManager(),
                                                        titles));

        slidingTabLayout.setViewPager(mViewPager);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }


}
