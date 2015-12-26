package com.timappweb.timapp.activities;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.Managers.SearchAndSelectTagManager;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.fragments.AddPostMainFragment;
import com.timappweb.timapp.fragments.AddPostSearchFragment;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class LocateActivity extends BaseActivity{

    private String TAG = "LocateActivity";

    // ---------------------------------------------------------------------------------------------
    private String                      comment = null;
    private Boolean                     dummyLocation = true;
    private Boolean                     noTags = true;
    private String                      mAddressOutput;


    // Views
    private TextView tvUserLocation;
    private ProgressBar progressBarLocation;
    private static ProgressDialog progressDialog = null;
    private LinearLayout addTagsLayout;
    private FragmentManager fragmentManager =   getFragmentManager();

    // Location
    private MyLocationProvider          locationProvider;
    private LocationListener            mLocationListener;
    private AddressResultReceiver       mResultReceiver;        // For reverse geocoding
    private SearchAndSelectTagManager searchAndSelectTagManager;


    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        this.initToolbar(true);

        initLocationListener();
        initLocationProvider();

        // -----------------------------------------------------------------------------------------
        // Init variables
        mResultReceiver = new AddressResultReceiver(new Handler());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationProvider.connect();
    }

    @Override
    protected void onStop() {
        locationProvider.disconnect();
        super.onStop();
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS
    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "User location has changed: " + Util.print(location));
                progressDialog.hide();
                startIntentServiceReverseGeocoding(location);
            }
        };
    }

    private void initLocationProvider() {
        locationProvider = new MyLocationProvider(this, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }
    }

    private void displayAddressOutput() {
        progressBarLocation.setVisibility(View.INVISIBLE);
        tvUserLocation.setText(this.mAddressOutput);
        // TODO update size of tvUserLoaction to match parent size

        //tvUserLocation.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void submitNewPost(){

        Location location = null;

        try{
            location = this.locationProvider.getLastLocation();
        }
        catch (NoLastLocationException ex) {
            Log.i(TAG, "Cannot get user location.");
            this.progressDialog.setMessage(getResources().getString(R.string.waiting_for_location));
            this.progressDialog.show();
            return;
            /*
            if (!BuildConfig.DEBUG) {
            }
            else if (dummyLocation){
                Log.i(TAG, "Debug mode. Using mock position.");
                String providerName = "";
                location = new Location(providerName);
                location.setLatitude(10);
                location.setLongitude(10);
                location.setAltitude(0);
                location.setTime(System.currentTimeMillis());
            }*/
        }


        //Location location = locationProvider.getLastLocation();
        // if precision sucks..
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "User position is: " + userLatLng + " with an accuracy of " + location.getAccuracy());
        // 3) Call the service to add the spot
        // - Build the spot
        final Post post = new Post(userLatLng);
        post.tag_string = getTagsToString();
        post.latitude = location.getLatitude();
        post.longitude = location.getLongitude();

        // Validating user input
        if (!post.validateForSubmit()){
            return;
        }
        Log.d(TAG, "Building spot: " + post);

        // Starting service
        this.progressDialog.setMessage(getResources().getString(R.string.please_wait));
        this.progressDialog.show();
        RestClient.service().addPost(post, new AddPostCallback(this, post));
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PROTECTED METHODS
    protected void startIntentServiceReverseGeocoding(Location location) {
        Log.d(TAG, "Starting IntentService to get use address from location");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    public void testClick(View view) {
        Intent intent = new Intent(this,TagActivity.class);
        startActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // INNER CLASSES

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            // Display the address string
            // or an error comment sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //displayAddressOutput();

            // Show a toast comment if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG);
            }

        }

    }

    private class AddPostCallback extends RestCallback<RestFeedback> {

        private final Post post;
        // TODO get post from server instead. (in case tags are in a black list)

        AddPostCallback(Context context, Post post) {
            super(context);
            this.post = post;
        }

        @Override
        public void success(RestFeedback restFeedback, Response response) {
            progressDialog.hide();

            if (restFeedback.success && restFeedback.data.containsKey("id")) {
                int id = Integer.valueOf(restFeedback.data.get("id"));
                Log.i(TAG, "Post has been saved. Id is : " + id);
                //Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot)
                IntentsUtils.post(this.context, id);
            } else {
                Log.i(TAG, "Cannot add spot: " + response.getReason() + " - " + restFeedback.toString());
                MyApplication.showAlert(this.context, restFeedback.message);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.hide();
            MyApplication.showAlert(this.context, R.string.error_webservice_connection);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // GETTERS AND SETTERS

    public String getTagsToString(){
        HorizontalTagsAdapter adapter = (HorizontalTagsAdapter)
                searchAndSelectTagManager.getSelectedTagsRecyclerView().getAdapter();
        String inputTags = "";
        List<Tag> selectedTags = adapter.getData();

        for (Tag tag: selectedTags){
            inputTags += tag.name + ",";
        }
        return inputTags;
    }



}
