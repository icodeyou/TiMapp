package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fragments.ExploreFragment;
import com.timappweb.timapp.utils.IntentsUtils;
//import android.support.design.widget.FloatingActionButton;


public class DrawerActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DrawerActivity";
    /* ============================================================================================*/
    /* PROPERTIES */
    /* ============================================================================================*/
    // Drawer
    private DrawerLayout mDrawerLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;
    private TextView tvUsername = null;
    private Toolbar toolbar;

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

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    //SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

     /* ============================================================================================*/
    /* ON CREATE */
    /* ============================================================================================*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //Toolbar
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
        View headerView = getLayoutInflater().inflate(R.layout.nav_header, nvDrawer, false);
        tvUsername = (TextView) headerView.findViewById(R.id.drawer_username);

        // -----------------------------------------------------------------------------------------
        final Activity that = this;
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.profile(that);
            }
        });

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
            Log.d(TAG, "Init add_spot_button button");
            addSpotFloatingButton.setOnClickListener(new AddSpotClickListener(this));
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

    /*  A QUOI CA SERT CA ???????????????????????????????????????????????????????????
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     *
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     *
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         *
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         *
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.useless_fragment_main, container, false);
            return rootView;
        }
    }
*/

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
        else if (id == R.id.menu_item_profile) {
            IntentsUtils.profile(this);
        }
        else if (id == R.id.menu_item_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menu_item_add_post){
            MyApplication.startRequireLoggedInActivity(this, AddSpotActivity.class);
        }
        else if (id == R.id.menu_item_login){
            IntentsUtils.login(this);
        }
        else if (id == R.id.menu_item_logout) {
            IntentsUtils.logout(this);
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
        // TODO check user access with RestClient.instance().checkToken();
        // TODO use constants

        switch (FragmentId.values()[position]){
            case Explore:             // DO THIS
                showAddSpotButton();
                newFragmentTAG = "Explore";
                newFragment = new ExploreFragment();
                break;

            default:            // By default go to Explore
                showAddSpotButton();
                newFragmentTAG = "Explore";
                newFragment = new ExploreFragment();
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
        navigationView.getMenu().findItem(R.id.menu_item_settings).setVisible(isLoggedIn);

        return res;
    }

    private class AddSpotClickListener implements View.OnClickListener {
        private final Activity activity;

        public AddSpotClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(View view) {
            MyApplication.startRequireLoggedInActivity(getBaseContext(), AddSpotActivity.class);
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

            if (MyApplication.isLoggedIn()){
                tvUsername.setText(MyApplication.getCurrentUser().username);
            }
            //getActionBar().setTitle(mDrawerTitle); TODO: application crash when used
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
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