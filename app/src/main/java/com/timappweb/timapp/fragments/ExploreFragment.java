package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;

public class ExploreFragment extends Fragment{

    private static final String TAG = "ExploreFragment";

    private AreaDataLoaderFromAPI dataLoader;


    private FrameLayout containerEvents;
    private View blurBackground;
    private ExploreEventsFragment eventsFragment;
    private ExploreMapFragment mapFragment;
    private View fab;

    private Menu menu;

    public ExploreMapFragment getExploreMapFragment(){
        return mapFragment;
    }

    public AreaDataLoaderFromAPI getDataLoader() {
        return dataLoader;
    }

    public AreaRequestHistory getAreaRequestHistory() {
        return getExploreMapFragment().getHistory();
    }

    /*
    public void reloadMapData(){
        if(getExploreMapFragment()!=null) {
            dataLoader.clear();
            getExploreMapFragment().updateMapData();
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        setHasOptionsMenu(true);

        containerEvents = (FrameLayout) root.findViewById(R.id.fragment_events);
        blurBackground = root.findViewById(R.id.blur_background);
        fab = root.findViewById(R.id.fab_button);

        dataLoader = new AreaDataLoaderFromAPI(this.getContext(), MyApplication.searchFilter);

        initAddSpotButton();
        initFragments();
        setListeners();

        return root;
    }

    public void initAddSpotButton() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.locate(getActivity());
            }
        });
    }

    private void initFragments() {
        //Map
        FragmentTransaction transactionMap = getChildFragmentManager().beginTransaction();
        mapFragment = new ExploreMapFragment();
        transactionMap.add(R.id.fragment_map, mapFragment);
        transactionMap.commit();

        //List
        FragmentTransaction transactionList = getChildFragmentManager().beginTransaction();
        eventsFragment = new ExploreEventsFragment();
        transactionList.add(R.id.fragment_events, eventsFragment);
        transactionList.commit();
    }

    private void setListeners() {
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

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                actionList();
                return true;
            case R.id.action_filter :
                IntentsUtils.filter(getActivity());
                return true;
            case R.id.action_clear_filter:
                MyApplication.searchFilter.tags.clear();
                getExploreMapFragment().updateFilterView();
                getExploreMapFragment().updateMapData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void actionList() {
        if(containerEvents.getVisibility()==View.GONE) {
            boolean isEventsOnMap = eventsFragment.onFragmentSelected(this);
            if(isEventsOnMap) {
                Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_down_all);
                Animation appear = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
                blurBackground.startAnimation(appear);
                containerEvents.startAnimation(slideIn);
                containerEvents.setVisibility(View.VISIBLE);
                blurBackground.setVisibility(View.VISIBLE);
                menu.findItem(R.id.action_list).setIcon(ContextCompat.getDrawable(getContext(), R.drawable.list_close));
            } else {
                Toast.makeText(getContext(), R.string.text_no_event, Toast.LENGTH_SHORT).show();
            }
        } else {
            Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_righ);
            Animation disappear = AnimationUtils.loadAnimation(getContext(), R.anim.disappear);
            blurBackground.startAnimation(disappear);
            containerEvents.startAnimation(slideOut);
            containerEvents.setVisibility(View.GONE);
            blurBackground.setVisibility(View.GONE);
            menu.findItem(R.id.action_list).setIcon(ContextCompat.getDrawable(getContext(), R.drawable.list));
        }
    }

    public void setFabVisibility(boolean isVisible) {
        fab.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public ExploreEventsFragment getExploreEventsFragment() {
        return eventsFragment;
    }
    public FrameLayout getContainerEvents() {
        return containerEvents;
    }

    /*public static class TabsAdapter extends FragmentPagerAdapter {

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

        *//**
         * This is only called when initializing the fragment
         * @param position
         * @return
         *//*
        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "TabsAdapter load position " + position);
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
                drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_map_light);
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
    }*/
}

