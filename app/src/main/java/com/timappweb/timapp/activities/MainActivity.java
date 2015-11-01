package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fragments.AddSpotFragment;
import com.timappweb.timapp.fragments.MapsFragment;

import java.util.Locale;
//import android.support.design.widget.FloatingActionButton;


public class MainActivity extends BaseActivity  {
    private static final String TAG = "LOG MAIN ACTIVITY";
    /* ============================================================================================*/
    /* PROPERTIES */
    /* ============================================================================================*/
    // Drawer
    private String[] mDrawerNavigationContent;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBarDrawerToggle mDrawerToggle;

    enum FragmentId{
        Map(0),
        AddSpot(1);
        private final int value;
        private FragmentId(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };
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
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
       // mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
      //  mViewPager = (ViewPager) findViewById(R.id.pager);
      //  mViewPager.setAdapter(mSectionsPagerAdapter);

        // !important Init drawer
        this.initDrawer();

        // !important Init drawer
        this.initAddSpotButton();

        if (savedInstanceState == null) {
            changeCurrentFragment(FragmentId.Map);
        }
    }

    /**
     * Create the button to add a spot
     */
    protected void initAddSpotButton() {
        if (addSpotFloatingButton == null){
            addSpotFloatingButton = (FloatingActionButton) findViewById(R.id.add_spot_button);
            Log.d(TAG, "Init add_spot_button button");
            addSpotFloatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),
                            "Open NewPost activity",
                            Toast.LENGTH_LONG).show();

                    changeCurrentFragment(FragmentId.AddSpot);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_login) {
            Intent intent = new Intent(this, LoginActivity.class);
        } else if (id == R.id.action_map){
            Intent intent = new Intent(this, MapsFragment.class);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }


    /* ============================================================================================*/
    /* DRAWER */
    /* ============================================================================================*/
    protected void initDrawer(){
        Log.d(TAG, "Drawer initialisation");

        mTitle = mDrawerTitle = getTitle();
        mDrawerNavigationContent = getResources().getStringArray(R.array.navigation_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer opens
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // --------------------------------
        // To close and open the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // ----------------------
        // enable ActionBar app icon to behave as action to toggle nav drawer
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        // ----------------------
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerNavigationContent));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            changeCurrentFragment(position);
        }
    }

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
        Fragment fragment = null;

        Log.i(TAG, "You clicked on button " + position);
        // TODO check user access with RestClient.instance().checkToken();
        // TODO use constants

        switch (FragmentId.values()[position]){
            case Map:             // DO THAT
                showAddSpotButton();
                fragment = new MapsFragment();
                break;
            case AddSpot:             // DO THIS
                if (!MyApplication.requireLoggedIn(this)){
                    return;
                }
                hideAddSpotButton();
                fragment = new AddSpotFragment();

                break;
            default:            // By default go to the map
                fragment = new MapsFragment();
        }

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();


        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerNavigationContent[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }

}
