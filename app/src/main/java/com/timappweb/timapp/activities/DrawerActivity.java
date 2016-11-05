package com.timappweb.timapp.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.paginate.CacheInfo;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.databinding.ActivityDrawerBinding;
import com.timappweb.timapp.fragments.ExploreFragment;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.listeners.FabListenerFactory;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import pl.aprilapps.easyphotopicker.EasyImage;


public class DrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, LocationManager.LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String         TAG                                 = "DrawerActivity";
    private static final String         EXPLORE_FRAGMENT_TAG                = "Explore";
    private static int                  TIMELAPSE_BEFORE_BACK_EXIT          = 2000;

    // ---------------------------------------------------------------------------------------------
    /* PROPERTIES */

    private DrawerLayout                mDrawerLayout;
    private CharSequence                mDrawerTitle;
    private CharSequence                mTitle;
    ActionBarDrawerToggle               mDrawerToggle;
    private Toolbar                     toolbar;
    private ExploreFragment             exploreFragment;

    private View fab;

    private boolean                     backPressedOnce;
    private FrameLayout                 mFrame;
    private View                        mWaitForLocationLayout;

    // ---------------------------------------------------------------------------------------------

    private static IntentFilter         syncIntentFilter            = new IntentFilter(DataSyncAdapter.ACTION_SYNC_EVENT_FINISHED);
    /*
    private BroadcastReceiver           syncBroadcastReceiver       = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // An event has been updated
            Log.d(TAG, "Sync finished, should refresh nao!!");
            EventStatusManager.updateCurrentEventStatus();
        }
    };*/
    private View cameraButton;
    private View tagButton;
    private View inviteButton;
    private View noEventLayout;
    private View eventLayout;
    private View eventBackground;
    private ActivityDrawerBinding mBinding;

    // ---------------------------------------------------------------------------------------------

    enum FragmentId{
        Explore(0), Settings(1);
        private final int value;
        private FragmentId(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    // ---------------------------------------------------------------------------------------------

    public void onDrawerTopClick(View view) {
        IntentsUtils.profile(this);
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        if (mWaitForLocationLayout != null){
            removeWaitForLocationLayout();
        }
    }

    private void removeWaitForLocationLayout() {
        ((ViewGroup)mWaitForLocationLayout.getParent()).removeView(mWaitForLocationLayout);
        mWaitForLocationLayout = null;
    }

    // -----------------------------------------------------------------------------------------
    // LIFE CALLBACKS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);
        //Import toolbar without calling function initToolbar, because of the toggle button
        toolbar = (Toolbar) findViewById(R.id.toolbar_id);
        mFrame = (FrameLayout) findViewById(R.id.content_frame);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Util.setStatusBarColor(this,R.color.status_bar_map);
        initDrawer();
        backPressedOnce = false;

        if (savedInstanceState == null) {
            changeCurrentFragment(FragmentId.Explore);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
        LocationManager.addOnLocationChangedListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Si on est dans la map => update Map Data // Liste => Update List Data.
        // TODO TEST
        //if (exploreFragment != null && exploreFragment.mapFragment != null && exploreFragment.mapFragment.isVisible()){
        //    exploreFragment.mapFragment.updateMapData();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        //EventBus.getDefault().register(this);
        updateEventViewInHeader();
        //registerReceiver(syncBroadcastReceiver, syncIntentFilter);
        if (!LocationManager.hasLastLocation() && mWaitForLocationLayout == null){
            mWaitForLocationLayout = getLayoutInflater().inflate(R.layout.waiting_for_location_map, null);
            Button skipLocation = (Button) mWaitForLocationLayout.findViewById(R.id.action_skip);
            skipLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "User skip location");
                    removeWaitForLocationLayout();
                }
            });
            mFrame.addView(mWaitForLocationLayout);
        }
        else if (LocationManager.hasLastLocation() && mWaitForLocationLayout != null){
            removeWaitForLocationLayout();
        }
    }

    @Override
    protected void onPause() {
        //unregisterReceiver(syncBroadcastReceiver);
        //EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocationManager.removeLocationListener(this);
        LocationManager.stop(this);
        super.onStop();
    }

    // -----------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (exploreFragment != null){
            ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
            View frameContainerEvent = exploreFragment.getContainerEvents();
            if(frameContainerEvent.getVisibility()==View.VISIBLE) {
                exploreFragment.actionList();
                return;
            }
            if(exploreMapFragment.isPlaceViewVisible()) {
                exploreMapFragment.hideEvent();
                return;
            }
        }

        if(mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START); //GravityCompat.START refers to the left drawer.
            return;
        }

        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.backPressedOnce = true;
        Toast.makeText(this, "Press back again to leave", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                backPressedOnce = false;
            }
        }, TIMELAPSE_BEFORE_BACK_EXIT);

    }


    /* ============================================================================================*/
    /* Methods */
    /* ============================================================================================*/

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean res = super.onPrepareOptionsMenu(menu);
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLayout);
        //menu.findItem(R.id.action_filter).setVisible(!drawerOpen);
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLayout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        boolean isLoggedIn = MyApplication.isLoggedIn();
        navigationView.getMenu().findItem(R.id.menu_item_profile).setVisible(isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_item_logout).setVisible(isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_item_login).setVisible(!isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_item_my_friends).setVisible(isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_item_settings).setVisible(false);
        navigationView.getMenu().findItem(R.id.menu_item_my_invitations).setVisible(isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_item_share).setVisible(isLoggedIn);
        navigationView.getMenu().findItem(R.id.menu_development_shortcut).setVisible(BuildConfig.DEBUG);

        MenuItem item = menu.findItem(R.id.action_clear_filter);
        if(exploreFragment != null) {
            if(exploreFragment.mapFragment!=null) {
                if(exploreFragment.mapFragment.isFilterActive()){
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
            }
        }

        return res;
    }

    public ExploreMapFragment getExploreMapFragment(){
        if (exploreFragment == null){
            return null;
        }
        return exploreFragment.getExploreMapFragment();
    }

    /* ============================================================================================*/
    /* DRAWER */
    /* ============================================================================================*/
    protected void initDrawer(){
        Log.d(TAG, "Drawer initialisation");

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new MyActionBarDrawerToggle(this,mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //hide scrollbar in drawer
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }

        eventBackground = findViewById(R.id.nav_background_event);
        noEventLayout = findViewById(R.id.no_events_layout);
        eventLayout = findViewById(R.id.event_layout);
    }

    private void updateEventViewInHeader() {
        final Event event = EventStatusManager.getCurrentEvent();
        //final Event event = null;
        mBinding.setEvent(event);
        if(event==null) {
            noEventLayout.setVisibility(View.VISIBLE);
            eventLayout.setVisibility(View.GONE);
        } else {
            noEventLayout.setVisibility(View.GONE);
            eventLayout.setVisibility(View.VISIBLE);

            View navView = findViewById(R.id.nav_view);
            if(navView != null) {
                FabListenerFactory.setFabListener(this, navView, event);
                View pictureBackgroundNav = navView.findViewById(R.id.nav_background_event);
                if(pictureBackgroundNav != null) {
                    pictureBackgroundNav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IntentsUtils.viewSpecifiedEvent(DrawerActivity.this, event);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    // ----------------------
    // Drawer menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id){
            case R.id.menu_item_viewpager:
                IntentsUtils.presentApp(this);
                break;
            case R.id.menu_item_explore:
                break;
            case R.id.menu_item_add_event:
                IntentsUtils.locate(this);
                break;
            case R.id.menu_item_my_invitations:
                IntentsUtils.invitations(this);
                break;
            case R.id.menu_item_my_friends:
                IntentsUtils.listFriends(this);
                break;
            case R.id.menu_item_share:
                IntentsUtils.share(this);
                break;
            case R.id.menu_item_profile:
                IntentsUtils.profile(this);
                break;
            case R.id.menu_item_settings:
                IntentsUtils.settings(this);
                break;
            case R.id.menu_item_login:
                IntentsUtils.login(this,false);
                break;
            case R.id.menu_item_logout:
                IntentsUtils.logout(this);
                finish();
                break;

            // DEV
            case R.id.menu_item_dummy_event:
                IntentsUtils.viewSpecifiedEvent(this, DummyEventFactory.create());
                break;
            case R.id.menu_item_dev_clear_config:
                ConfigurationProvider.clearAll();
                MyApplication.restart(this);
                finish();
                break;
            case R.id.menu_item_dev_clear_cache:
                SQLite.delete().from(CacheInfo.class).execute();
                break;
            case R.id.menu_item_test_cam:
                EasyImage.openCamera(this, 0);
                break;
            case R.id.menu_item_crash_app:
                throw new RuntimeException("Simulate crash!");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ----------
    //Manage fragments in drawer
    private void changeCurrentFragment(FragmentId id) {
        changeCurrentFragment(id.getValue());
    }
    /**
     * Swaps fragments in the main content view
     *
     * TODO remove !!!
     * @param position
     */
    private void changeCurrentFragment(int position) {
        // Create a new fragment according to the clicked item
        FragmentManager fragmentManager = getSupportFragmentManager();

        Log.i(TAG, "changeCurrentFragment() position=" + position);

        exploreFragment = new ExploreFragment();

        //Set The action bar Title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(EXPLORE_FRAGMENT_TAG);
        }
        // Insert the fragment by replacing any existing fragment,
        // only if the asked fragment isn't the same as the current fragment
        //if (currentFragmentTAG != newFragmentTAG) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, exploreFragment, EXPLORE_FRAGMENT_TAG).commit();
        //}
    }


    private class MyActionBarDrawerToggle extends ActionBarDrawerToggle {
        public MyActionBarDrawerToggle(DrawerActivity drawerActivity, DrawerLayout mDrawerLayout, Toolbar toolbar, int drawer_open, int drawer_close) {
            super(drawerActivity, mDrawerLayout, toolbar, drawer_open, drawer_close);
        }

        /** Called when a drawer has settled in a completely closed state. */
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

            /** Called when a drawer has settled in a completely open state. */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

}