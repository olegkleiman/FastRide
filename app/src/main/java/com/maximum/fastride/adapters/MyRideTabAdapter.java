package com.maximum.fastride.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.maximum.fastride.MyRides.GeneralMyRidesFragment;
import com.maximum.fastride.MyRides.RejectedMyRidesFragment;


/**
 * Created by eli max on 18/06/2015.
 */
public class MyRideTabAdapter extends FragmentPagerAdapter {

    private String titles[];

    public MyRideTabAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {

//        switch( position) {
//            case 0:
//                return GeneralMyRidesFragment.newInstance(position);
//
//            case 1:
//                return RejectedMyRidesFragment.newInstance(position);
//        }

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

