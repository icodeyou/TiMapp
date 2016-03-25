package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;

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

    public AreaRequestHistory getAreaRequestHistory() {
        return getExploreMapFragment().getHistory();
    }

    public void reloadMapData(){
        if(getExploreMapFragment()!=null) {
            dataLoader.clear();
            getExploreMapFragment().updateMapData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);

        /** Important: Must use the child FragmentManager or you will see side effects. */
        tabsAdapter = new TabsAdapter(getChildFragmentManager());

        viewPager = (ViewPager) root.findViewById(R.id.explore_viewpager);
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        viewPager.setAdapter(tabsAdapter);

        dataLoader = new AreaDataLoaderFromAPI(this.getContext(), this, MyApplication.searchFilter);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) root.findViewById(R.id.pager_tab_strip);

        //hide underline
        pagerTabStrip.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        pagerTabStrip.setTextColor(ContextCompat.getColor(getContext(),R.color.White));
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(getContext(),R.color.colorSecondary));


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
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

        /**
         * This is only called when initializing the fragment
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "TabsAdapter init position " + position);
            if (position == 0) {
                if (exploreMapFragment == null) exploreMapFragment = new ExploreMapFragment();
                return exploreMapFragment;
            } else {
                if (explorePlacesFragment == null) explorePlacesFragment = new ExplorePlacesFragment();
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

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, "onPageScrolled: " + position);
            ((OnExploreTabSelectedListener)tabsAdapter.getItem(position)).onTabSelected();
        }

        @Override
        public void onPageSelected(int position) {
            //Log.d(TAG, "Page is now selected: " + position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.d(TAG, "onPageScrollStateChanged: " + state);
        }
    }
}

