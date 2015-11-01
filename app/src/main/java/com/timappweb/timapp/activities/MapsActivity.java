package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.fragments.MapsFragment;
import com.timappweb.timapp.fragments.SpotItemFragment;
import com.timappweb.timapp.fragments.SpotsTagFragment;

import java.util.Map;


/**
 * @warnings DO NOT USE THIS CLASS. TO BE REMOVED
 * TODO remove this class
 */
public class MapsActivity extends AppCompatActivity implements SpotItemFragment.OnFragmentInteractionListener {

    private static final String TAG = "MapActivity";

    public static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        /*
        fragmentManager = getSupportFragmentManager();
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_fragment);
        if (map == null){
            Log.e(TAG, "Error map is null");
        }
        */


    }

    public static SpotItemFragment spotItemFragment = null;
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof SpotItemFragment){
            Toast.makeText(getApplicationContext(), String.valueOf(fragment.getId()), Toast.LENGTH_SHORT).show();
            MapsActivity.spotItemFragment = (SpotItemFragment) fragment;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_broadcast){
            Intent intent = new Intent(this, BroadcastActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
