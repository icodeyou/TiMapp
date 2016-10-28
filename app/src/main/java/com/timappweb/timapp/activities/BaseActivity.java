package com.timappweb.timapp.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.timappweb.timapp.R;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG     = "BaseActivity";
    protected SearchView                searchView;
    public Toolbar mToolbar;

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


    protected void initToolbar(boolean showTitle, int arrowColor) {
        initToolbar(showTitle);
        //gradle v24 : abc_ic_ab_back_material /
        // Before: abc_ic_ab_back_mtrl_am_alpha @see http://stackoverflow.com/questions/35632576/error-cannot-find-symbol-variable-abc-ic-ab-back-mtrl-am-alpha
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material, null);
        upArrow.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    protected void setSearchview(Menu menu) {
        //Set search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        //Always display the searchview expanded in the action bar
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        //set searchView
        searchView = (SearchView) searchItem.getActionView();
    }

}