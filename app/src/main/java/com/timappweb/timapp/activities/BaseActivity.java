package com.timappweb.timapp.activities;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.MyLocationProvider;

public class BaseActivity extends AppCompatActivity {

    protected SearchView searchView;
    private MyLocationProvider          locationProvider;

    protected void enableGPS(){
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    protected void initLocationProvider(LocationListener mLocationListener) {
        locationProvider = new MyLocationProvider(this, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }

        locationProvider.requestMultipleUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationProvider != null){
            locationProvider.connect();
        }
    }

    @Override
    protected void onStop() {
        if (locationProvider != null){
            locationProvider.disconnect();
        }
        super.onStop();
    }

    protected void initToolbar(boolean showTitle){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar==null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar_transparent);
        }
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(showTitle);
    }

    protected void setSimpleTouchListener(View button, final int resource) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setBackgroundResource(resource);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setBackground(null);
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    protected void setRadiusTouchListener(View button, final TextView tv) {
        button.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary));
                        v.setBackgroundResource(R.drawable.background_radius_selected);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_button));
                        v.setBackground(null);
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    protected void setRadiusTouchListener(View button, final TextView tv1, final TextView tv2) {
        button.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setBackgroundResource(R.drawable.background_radius_selected);
                        tv1.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary));
                        tv2.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary));
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setBackground(null);
                        tv1.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_button));
                        tv2.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_button));
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    protected void setSquareTouchListener(final View button, final TextView tv) {
        button.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setBackgroundResource(R.drawable.background_button_selected);
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorSecondary));
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setBackgroundResource(R.drawable.background_button);
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_button));
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }



    protected void setSearchview(Menu menu) {
        //Set search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();
        final BaseActivity that = this;

        //Always display the searchview expanded in the action bar
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                NavUtils.navigateUpFromSameTask(that);
                return false;
            }
        });

        //set searchView
        searchView = (SearchView) searchItem.getActionView();

        //This doesn't work. There isn't any way to remove the suggestions.
        //searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        ////searchView.setImeOptions(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);


        ////////////////// Test Get EditText in SearchView and handle on Space event
        //////////////////////////////////////////////////////////////////////////
        /*int searchPlateId = getResources().getIdentifier("android:id/search_src_text", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate!=null) {
            searchPlate.setBackgroundColor(Color.CYAN);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.BLACK);
                searchText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_SPACE)) {
                            Log.d("youpi", "ohyeah");
                        }
                        return true;
                    }
                });
            }
        }*/
    }

}