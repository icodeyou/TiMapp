package com.timappweb.timapp.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.timappweb.timapp.R;
import com.timappweb.timapp.fragments.MoodLocationCityFragment;
import com.timappweb.timapp.fragments.MoodLocationCountryFragment;
import com.timappweb.timapp.fragments.MoodLocationMainFragment;

public class MoodLocationActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_location);
        changeCurrentFragment("Menu");

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Choose a location");
    }

    private void changeCurrentFragment(String id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        switch (id) {
            case "Menu":             // DO THIS
                fragment = new MoodLocationMainFragment();
                break;
            case "SelectCountry":
                fragment = new MoodLocationCountryFragment();
                break;
            case "SelectCity":
                fragment = new MoodLocationCityFragment();
                break;
            default:
                fragment =new MoodLocationMainFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
    //mood_location_frame
}
