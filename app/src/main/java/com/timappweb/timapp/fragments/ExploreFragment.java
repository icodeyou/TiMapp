package com.timappweb.timapp.fragments;

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

public class ExploreFragment extends Fragment{

    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;

    public ExploreMapFragment getExploreMapFragment(){
        return tabsAdapter.getExploreMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        Log.d("ExploreFragment", "View is created");
        viewPager = (ViewPager) root.findViewById(R.id.explore_viewpager);
        PagerTabStrip pagerTabStrip = (PagerTabStrip) root.findViewById(R.id.pager_tab_strip);
        /** Important: Must use the child FragmentManager or you will see side effects. */
        this.tabsAdapter =new TabsAdapter(getChildFragmentManager());
        viewPager.setAdapter(this.tabsAdapter);

        //hide underline
        pagerTabStrip.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        pagerTabStrip.setTextColor(getResources().getColor(R.color.White));
        pagerTabStrip.setDrawFullUnderline(false);

        return root;
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

    public static class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        public ExploreMapFragment getExploreMapFragment() {
            return exploreMapFragment;
        }

        private ExploreMapFragment exploreMapFragment;

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                exploreMapFragment = new ExploreMapFragment();
                return exploreMapFragment;
            } else {
                return new ExplorePlacesFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "MAP";
            }
            else {
                return "EVENTS";
            }
        }
    }
}

