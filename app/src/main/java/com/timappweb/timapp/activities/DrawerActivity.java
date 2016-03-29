package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
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

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fragments.ExploreFragment;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.fragments.ExploreMapFragment;
//import android.support.design.widget.FloatingActionButton;


public class DrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DrawerActivity";
    /* ============================================================================================*/
    /* PROPERTIES */
    /* =========================================
    ===================================================*/
    // Drawer
    private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private ExploreFragment exploreFragment;

    private SimpleFacebook mSimpleFacebook;
    private OnLogoutListener onLogoutListener;

    public void onDrawerTopClick(View view) {
        IntentsUtils.profile(this);
    }

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

    /* ============================================================================================*/
    // Add spot button
    FloatingActionButton addSpotFloatingButton = null;
    /* ============================================================================================*/

     /* ============================================================================================*/
    /* ON CREATE */
    /* ============================================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //Import toolbar without calling function initToolbar, because of the toggle button
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        // mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        //  mViewPager = (ViewPager) findViewById(R.id.pager);
        //  mViewPager.setAdapter(mSectionsPagerAdapter);

        // !important Init drawer
        this.initDrawer();

        // !important Init AddButton
        this.initAddSpotButton();

        if (savedInstanceState == null) {
            changeCurrentFragment(FragmentId.Explore);
        }

        // -----------------------------------------------------------------------------------------

        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
        //hide scrollbar in drawer
        NavigationMenuView navigationMenuView = (NavigationMenuView) nvDrawer.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }
        //inflate header
        getLayoutInflater().inflate(R.layout.nav_header, nvDrawer, false);

        // -----------------------------------------------------------------------------------------
        setListeners();

    }

    private void setListeners() {
        final Activity that = this;

        onLogoutListener = new OnLogoutListener() {
            @Override
            public void onLogout() {
                Log.i(TAG, "You are logged out");
            }
        };
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(exploreFragment!=null) {
            exploreFragment.reloadMapData(); //Reload data
            exploreFragment.getExploreMapFragment().updateFilterView(); //Set Filter view above map.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (exploreFragment != null){
            ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
            if (exploreFragment.getCurrentItem()==0 && exploreMapFragment.isPlacesViewerVisible()) {
                exploreMapFragment.hidePlace();
            }
            else{
                super.onBackPressed();
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
        if (addSpotFloatingButton == null){
            addSpotFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
            final Activity that = this;
            Log.d(TAG, "Init add_spot_button button");
            addSpotFloatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPostStepLocate(that);
                }
            });
        }
    }
    protected void hideAddSpotButton(){
        addSpotFloatingButton.setVisibility(View.GONE);
    }
    protected void showAddSpotButton(){
        addSpotFloatingButton.setVisibility(View.VISIBLE);
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
        if (id == R.id.action_filter) {
            IntentsUtils.filter(this);
        }

        return super.onOptionsItemSelected(item);
    }

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

        if (id == R.id.menu_item_explore) {
            IntentsUtils.home(this);
            //changeCurrentFragment(FragmentId.Explore);
        }
        else if (id == R.id.menu_item_tag_around){
            IntentsUtils.addPostStepLocate(this);
        }
        else if (id == R.id.menu_item_my_friends){
            IntentsUtils.listFriends(this);
        }
        else if (id == R.id.menu_item_share){
            IntentsUtils.share(this);
        }
        else if (id == R.id.menu_item_profile) {
            IntentsUtils.profile(this);
        }
        else if (id == R.id.menu_item_settings) {
            IntentsUtils.settings(this);
            //IntentsUtils.addPeople(this);
        }
        else if (id == R.id.menu_item_login){
            IntentsUtils.login(this);
        }
        else if (id == R.id.menu_item_logout) {
            IntentsUtils.logout(this);
            mSimpleFacebook.logout(onLogoutListener);
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
        if (currentFragmentTAG != newFragmentTAG) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, newFragment, newFragmentTAG).commit();
        }

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

    public void setFabOpacity(float opacity) {
        addSpotFloatingButton.setAlpha(opacity);
    }

    /////////////////////////////////////////////////////////
    //////////////// FRAGMENTS /////////////////////////////

    // MoodFragment /////////////////////////
 /*
    public void onMoodLocationClick(View view) {
        Intent chooseLocation = new Intent(this, MoodLocationActivity.class);
        startActivity(chooseLocation);
    }
*/

}