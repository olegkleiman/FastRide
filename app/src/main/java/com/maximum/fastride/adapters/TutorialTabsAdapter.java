package com.maximum.fastride.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.maximum.fastride.tutorials.DriverTutorialFragment;
import com.maximum.fastride.tutorials.IntroTutorialFragment;
import com.maximum.fastride.tutorials.PassengerRoleTutorialFragment;
import com.maximum.fastride.tutorials.ReclaimTutorialFragment;

/**
 * Created by Oleg on 16-May-15.
 */
public class TutorialTabsAdapter extends FragmentPagerAdapter {

    private String titles[];

    public TutorialTabsAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {

        switch( position) {
            case 0:
                return IntroTutorialFragment.newInstance(position);

            case 1:
                return DriverTutorialFragment.newInstance(position);

            case 2:
                return PassengerRoleTutorialFragment.newInstance(position);

            case 3:
                return ReclaimTutorialFragment.newInstance(position);

        }

        return  null;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}