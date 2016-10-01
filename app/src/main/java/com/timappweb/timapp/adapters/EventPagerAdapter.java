package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;


import com.timappweb.timapp.fragments.EventBaseFragment;

import java.util.List;

public class EventPagerAdapter extends FragmentPagerAdapter {

    private final List<EventBaseFragment> fragments;
    private final Context context;

    public EventPagerAdapter(Context context, FragmentManager fm, List<EventBaseFragment> fragments) {
        super(fm);
        this.context = context;
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
        return context.getString(fragments.get(position).getTitle());
    }
}