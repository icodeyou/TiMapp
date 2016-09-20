package com.timappweb.timapp.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.MyLocationProvider;

import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG     = "BaseActivity";
    protected SearchView                searchView;
    private MyLocationProvider          locationProvider;
    protected List<Call>                apiCalls = new LinkedList<>();
    public Toolbar mToolbar;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "BaseActivity::onDestroy()");
        for (Call call: apiCalls){
            call.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void initToolbar(boolean showTitle){
        mToolbar = (Toolbar) findViewById(R.id.toolbar_id);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(showTitle);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setHomeButtonEnabled(true);
    }

    public void showActionBar() {
        this.getSupportActionBar().show();;
    }

    public void hideActionBar() {
        this.getSupportActionBar().hide();;
    }


    protected void initToolbar(boolean showTitle, int arrowColor) {
        initToolbar(showTitle);
        //gradle v24 : abc_ic_ab_back_material
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        upArrow.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    protected void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    public void initMapUI(GoogleMap gMap, boolean gestureEnabled) {
        gMap.setIndoorEnabled(true);
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setScrollGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setRotateGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setTiltGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setMapToolbarEnabled(false);
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
    }

}