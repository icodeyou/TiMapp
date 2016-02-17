package com.timappweb.timapp.fragments;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;

public class ExploreFragment extends Fragment{

    private static final String TAG = "ExploreFragment";

    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;
    private AreaDataLoaderFromAPI dataLoader;

    public ExploreMapFragment getExploreMapFragment(){
        return tabsAdapter.getExploreMapFragment();
    }

    public AreaDataLoaderFromAPI getDataLoader() {
        return dataLoader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.fragment_explore, container, false);

        viewPager = (ViewPager) root.findViewById(R.id.explore_viewpager);
        PagerTabStrip pagerTabStrip = (PagerTabStrip) root.findViewById(R.id.pager_tab_strip);
        /** Important: Must use the child FragmentManager or you will see side effects. */
        this.tabsAdapter = new TabsAdapter(getChildFragmentManager());
        viewPager.setAdapter(this.tabsAdapter);
        //hide underline
        pagerTabStrip.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        pagerTabStrip.setTextColor(getResources().getColor(R.color.White));
        pagerTabStrip.setDrawFullUnderline(false);

        this.dataLoader = new AreaDataLoaderFromAPI(this.getContext(), this);

        return root;
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public static class TabsAdapter extends FragmentPagerAdapter {

        private ExploreMapFragment exploreMapFragment;
        private ExplorePlacesFragment explorePlacesFragment;

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        public ExploreMapFragment getExploreMapFragment() {
            return exploreMapFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "TabsAdapter init position " + position);
            if (position == 0) {
                exploreMapFragment = new ExploreMapFragment();
                return exploreMapFragment;
            } else {
                explorePlacesFragment = new ExplorePlacesFragment();
                return explorePlacesFragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Map";
            }
            else {
                return "List";
            }
        }
    }
}

