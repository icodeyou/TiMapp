package com.timappweb.timapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.LocationListener;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.MyLocationProvider;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG     = "BaseActivity";
    protected SearchView                searchView;
    private MyLocationProvider          locationProvider;
    private SimpleFacebook              mSimpleFacebook;
    private OnFriendsListener           onFriendsListener;
    protected List<Call>                apiCalls = new LinkedList<>();


    @Override
    protected void onDestroy() {
        Log.d(TAG, "BaseActivity::onDestroy()");
        for (Call call: apiCalls){
            call.cancel();
        }
        super.onDestroy();
    }

    protected void enableGPS(){
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    public void initLocationProvider(LocationListener mLocationListener) {
        locationProvider = new MyLocationProvider(this, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }

        locationProvider.requestMultipleUpdates();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSimpleFacebook != null){
            mSimpleFacebook = SimpleFacebook.getInstance(this);
            setAllFbFriends();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSimpleFacebook != null){
            mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    protected void initToolbar(boolean showTitle, int arrowColor) {
        initToolbar(showTitle);
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        upArrow.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    protected void getFriends(OnFriendsListener onFriendsListener){
            this.onFriendsListener = onFriendsListener;
    }

    private void setAllFbFriends() {
        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(100);
        pictureAttributes.setWidth(100);
        pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);

        // Set the properties that you want to get
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.NAME)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        mSimpleFacebook.getFriends(properties, onFriendsListener);
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