package com.timappweb.timapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.RestFeedback;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.fragments.AlertDialog;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.Feedback;
import com.timappweb.timapp.utils.MyLocationListener;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;

import retrofit.client.Response;

public class AddSpotActivity extends BaseActivity {

    private static final String TAG = "AddSpot";
    private OnFragmentInteractionListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating add spot activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Broadcast");

        mResultReceiver = new AddressResultReceiver(new Handler());
        this.dialog = new ProgressDialog(this);
        this.dialog.setMessage("Please wait...");

        this.lp = new MyLocationProvider(this);
        this.lp.getUserLocation(mLocationListener);

        Log.d(TAG, "Location provider has been setBounds up: " + lp);

        Button b = (Button) findViewById(R.id.button_submit_spot);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                broadcastPosition();
            }
        });

    }

    // UP NAVIGATION - Action Bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public void onAttach(Activity activity) {
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.d(TAG, activity.toString()
                    + " must implement OnFragmentInteractionListener");
            //throw new ClassCastException(activity.toString()
            //        + " must implement OnFragmentInteractionListener");
        }
    }

    public void onDetach() {
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }






    ProgressDialog dialog;


    public void broadcastPosition(){
        // 1) Get the user position
        // if (! this.lp.hasKnownPosition()){
        Location location;
        try{
            location = this.lp.getLastLocation();
        }
        catch (NoLastLocationException ex){
            Log.i(TAG, "Cannot get position.");
            AlertDialog.show(this, R.string.error_cannot_get_location);
            return;
        }

        //Location location = lp.getLastLocation();
        // if precision sucks..
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "User position is: " + ll + " with an accuracy of " + location.getAccuracy());

        // 2) Get input tags
        String inputTags = ((EditText) findViewById(R.id.input_tags)).getText().toString();
        Log.d(TAG, "User tags: " + inputTags);

        // 3) Call the service to add the spot
        // - Build the spot
        final Spot spot = new Spot(ll);
        spot.tag_string = inputTags;
        Log.d(TAG, "Building spot: " + spot);
        RestClient.instance().getService().addSpot(spot, new RestCallback<RestFeedback>() {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                dialog.hide();

                if (restFeedback.success){
                    Log.i(TAG, "Request post success: " + response);
                    Object o = restFeedback.data.get("expired");
                    Log.i(TAG, "-->" + o);
                    spot.tag_string = restFeedback.data.get("tag_string");
                    //spot.setExpired(Long.valueOf(restFeedback.data.get("expired")));
                    spot.setCreated(Integer.valueOf(restFeedback.data.get("created"))); // TODO check exception ClassCastException + NullPointerException
                    spot.writeToPref();
                    Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot);
                    dialog.hide();

                    // TODO change view
                    //Intent intent = new Intent(getActivity().getApplicationContext(), CurrentSpotActivity.class);
                    //startActivity(intent);
                }
                else{
                    Log.i(TAG, "Cannot add spot: " + response);
                    Feedback.show(getApplicationContext(), R.string.error_webservice_connection);
                }
            }

        });
    }

    private final LocationListener mLocationListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.i(TAG, "User location has changed: " + Util.print(location));
            startIntentService(location);
        }
    };
    private MyLocationProvider lp;

    private AddressResultReceiver mResultReceiver;

    protected void startIntentService(Location location) {
        Log.d(TAG, "Starting IntentService to get use adress from location");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void displayAddressOutput() {
        TextView tvUserLocation = (TextView) findViewById(R.id.tv_user_location);
        tvUserLocation.setText(this.mAddressOutput);
    }

    private String mAddressOutput;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG);
            }

        }

    }
}