package com.timappweb.timapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.timappweb.timapp.views.parallaxviewpager.ParallaxFragmentPagerAdapter;

import java.util.List;

public class EventPagerAdapter extends ParallaxFragmentPagerAdapter {

    private final List<Fragment> fragments;

    public EventPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, fragments.size());
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
        /*
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = FirstScrollViewFragment.newInstance(0);
                break;

            case 1:
                fragment = SecondScrollViewFragment.newInstance(1);
                break;

            case 2:
                fragment = DemoListViewFragment.newInstance(2);
                break;
            default:
                throw new IllegalArgumentException("Wrong page given " + position);
        }
        return fragment;*/

    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Info";
            case 1:
                return "Pictures";
            case 2:
                return "Tags";
            case 3:
                return "People";
            default:
                return "Other";
        }
    }
}