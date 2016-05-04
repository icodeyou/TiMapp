package com.timappweb.timapp.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.listeners.OnExploreTabSelectedListener;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;

public class ExploreFragment extends Fragment{

    private static final String TAG = "ExploreFragment";

    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;
    private AreaDataLoaderFromAPI dataLoader;
    private MyOnPageChangeListener onPageChangeListener;

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
        tabsAdapter = new TabsAdapter(getContext(), getChildFragmentManager());

        viewPager = (ViewPager) root.findViewById(R.id.explore_viewpager);
        onPageChangeListener =new MyOnPageChangeListener();
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setAdapter(tabsAdapter);
        dataLoader = new AreaDataLoaderFromAPI(this.getContext(), MyApplication.searchFilter);
        dataLoader.setLoadingListener(new LoadingListener() {
            @Override
            public void onLoadStart() {
                if (getExploreMapFragment() != null) getExploreMapFragment().setLoaderVisibility(true);
            }

            @Override
            public void onLoadEnd() {
                if (getExploreMapFragment() != null) getExploreMapFragment().setLoaderVisibility(false);
            }
        });


        PagerTabStrip pagerTabStrip = (PagerTabStrip) root.findViewById(R.id.pager_tab_strip);

        //hide underline
        pagerTabStrip.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        pagerTabStrip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Fragment getFragmentSelected() {
        return tabsAdapter.getItem(viewPager.getCurrentItem());
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public static class TabsAdapter extends FragmentPagerAdapter {

        private ExploreMapFragment exploreMapFragment;
        private ExploreEventsFragment exploreEventsFragment;
        private Context mContext;

        public TabsAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.mContext = context;
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
                if (exploreEventsFragment == null) exploreEventsFragment = new ExploreEventsFragment();
                return exploreEventsFragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SpannableStringBuilder sb;
            Drawable drawable;
            if (position == 0) {
                sb = new SpannableStringBuilder(" " + mContext.getString(R.string.tab_map));
                drawable = ContextCompat.getDrawable(mContext, android.R.drawable.ic_dialog_map);
            }
            else {
                sb = new SpannableStringBuilder(" " + mContext.getString(R.string.tab_list));
                drawable = ContextCompat.getDrawable(mContext, R.drawable.list);
            }
            int icSize = (int) mContext.getResources().getDimension(R.dimen.logo_small);
            drawable.setBounds(0, 0, icSize, icSize);
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            return sb;
        }

    }

    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //Log.d(TAG, "onPageScrolled: " + position);
        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "Page is now selected: " + position);
            ((OnExploreTabSelectedListener)tabsAdapter.getItem(position)).onTabSelected();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.d(TAG, "onPageScrollStateChanged: " + state);
        }
    }
}

