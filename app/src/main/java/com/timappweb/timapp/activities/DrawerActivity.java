package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.fragments.ExploreFragment;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.location.LocationManager;
//import android.support.design.widget.FloatingActionButton;


public class DrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, LocationManager.LocationListener {

    private static final String         TAG                                 = "DrawerActivity";
    private static int                  TIMELAPSE_BEFORE_BACK_EXIT          = 2000;

    // ---------------------------------------------------------------------------------------------
    /* PROPERTIES */

    private DrawerLayout                mDrawerLayout;
    private CharSequence                mDrawerTitle;
    private CharSequence                mTitle;
    ActionBarDrawerToggle               mDrawerToggle;
    private Toolbar                     toolbar;
    private ExploreFragment             exploreFragment;

    private SimpleFacebook              mSimpleFacebook;
    private View                        fabContainer;

    private boolean                     backPressedOnce;
    private FrameLayout                 mFrame;
    private View                        mWaitForLocationLayout;

    // ---------------------------------------------------------------------------------------------

    private static IntentFilter         syncIntentFilter            = new IntentFilter(DataSyncAdapter.ACTION_SYNC_EVENT_FINISHED);
    private BroadcastReceiver           syncBroadcastReceiver       = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // An event has been updated
            Log.d(TAG, "Sync finished, should refresh nao!!");
            EventStatusManager.updateCurrentEventStatus();
        }
    };

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
        if (lastLocation == null){
            //updateMapData();
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
        setContentView(R.layout.activity_drawer);

        //Import toolbar without calling function initToolbar, because of the toggle button
        toolbar = (Toolbar) findViewById(R.id.toolbar_id);
        mFrame = (FrameLayout) findViewById(R.id.content_frame);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setStatusBarColor(R.color.status_bar_map);
        initDrawer();
        this.initAddSpotButton();
        this.initList();
        backPressedOnce = false;

        if (savedInstanceState == null) {
            changeCurrentFragment(FragmentId.Explore);
        }

        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
        //hide scrollbar in drawer
        NavigationMenuView navigationMenuView = (NavigationMenuView) nvDrawer.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }
        //inflate header
        getLayoutInflater().inflate(R.layout.nav_header, nvDrawer, false);
        LocationManager.addOnLocationChangedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //updateMapData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncBroadcastReceiver, syncIntentFilter);
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        if (!LocationManager.hasLastLocation() && mWaitForLocationLayout == null){
            mWaitForLocationLayout = getLayoutInflater().inflate(R.layout.waiting_for_location, null);
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
    }

    @Override
    protected void onPause() {
        unregisterReceiver(syncBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // -----------------------------------------------------------------------------------------

    private void initList() {
        ImageView listIcon = (ImageView) findViewById(R.id.list_icon);
        listIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exploreFragment.updateList();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (exploreFragment != null){
            ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
            if (exploreFragment.getFragmentSelected() instanceof ExploreMapFragment
                    && exploreMapFragment.isPlaceViewVisible()) {
                exploreMapFragment.hideEvent();
            }
            else{
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
        }
        else {
            super.onBackPressed();
        }
    }


    /* ============================================================================================*/
    /* Methods */
    /* ============================================================================================*/

    /**
     * Create the button to add a spot
     */
    protected void initAddSpotButton() {
        fabContainer = findViewById(R.id.fab_container);
        final Activity that = this;
        Log.d(TAG, "Init add_spot_button button");
        fabContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.locate(that);
            }
        });
    }
    protected void hideAddSpotButton(){
        fabContainer.setVisibility(View.GONE);
    }
    protected void showAddSpotButton(){
        fabContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_filter :
                IntentsUtils.filter(this);
                break;
            case R.id.action_clear_filter:
                MyApplication.searchFilter.tags.clear();
                exploreFragment.getExploreMapFragment().updateFilterView();
                exploreFragment.getExploreMapFragment().updateMapData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean res = super.onPrepareOptionsMenu(menu);
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLayout);
        //menu.findItem(R.remote_id.action_filter).setVisible(!drawerOpen);
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

        MenuItem item = menu.findItem(R.id.action_clear_filter);
        if(exploreFragment != null) {
            if(exploreFragment.getExploreMapFragment()!=null) {
                if(exploreFragment.getExploreMapFragment().isFilterActive()){
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
            }
        }

        return res;
    }


    /* ============================================================================================*/
    /* DRAWER */
    /* ============================================================================================*/
    protected void initDrawer(){
        Log.d(TAG, "Drawer initialisation");

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer opens
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // --------------------------------
        // To close and open the drawer
        mDrawerToggle = new MyActionBarDrawerToggle(this,mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // ----------------------
    // Drawer menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id){
            case R.id.menu_item_explore:
                IntentsUtils.home(this);
                break;
            case R.id.menu_item_tag_around:
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
                IntentsUtils.login(this);
                break;
            case R.id.menu_item_logout:
                IntentsUtils.logout(this);
                mSimpleFacebook.logout(new OnLogoutListener() {
                    @Override
                    public void onLogout() {
                    Log.i(TAG, "You are logged out");
                    }
                });
                finish();
                break;
            // DEV
            case R.id.menu_item_dummy_event:
                IntentsUtils.viewSpecifiedEvent(this, Event.createDummy());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // ----------
    //Manage fragments in drawer
    private void changeCurrentFragment(FragmentId id) {
        changeCurrentFragment(id.getValue());
    }
    /**
     * Swaps fragments in the main content view
     * @param position
     */
    private void changeCurrentFragment(int position) {
        // Create a new fragment according to the clicked item
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment newFragment = null;
        String newFragmentTAG = "Explore";

        Log.i(TAG, "You clicked on button " + position);

        switch (FragmentId.values()[position]){
            default:            // By default go to Explore
                showAddSpotButton();
                newFragmentTAG = "Explore";
                newFragment = new ExploreFragment();
                exploreFragment = (ExploreFragment) newFragment;
        }

        //Set The action bar Title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newFragmentTAG);
        }

        // Get TAG of current fragment
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
        String currentFragmentTAG = null;
        if (currentFragment != null) {
            currentFragmentTAG = currentFragment.getTag();
        }

        // Insert the fragment by replacing any existing fragment,
        // only if the asked fragment isn't the same as the current fragment
        //if (currentFragmentTAG != newFragmentTAG) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, newFragment, newFragmentTAG).commit();
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

    public void updateFabPosition(ListView placesViewer) {
        int padding = placesViewer.getHeight();
        fabContainer.setPadding(0,0,0,padding);
    }

    public void clearFabPosition() {
        fabContainer.setPadding(0,0,0,0);
    }


}