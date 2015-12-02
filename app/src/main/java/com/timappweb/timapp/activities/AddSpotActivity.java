package com.timappweb.timapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.timappweb.timapp.R;

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
/*
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
*/


        //Import results into the vertical ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        ListView lv = (ListView) findViewById(R.id.suggested_tags);

        //Example of tags :
        String[] tags_ex = {"hilarious", "despicable", "OKLM", "yeah",
                "whynot","ridiculous","good","awful","sexdrugsandrocknroll"};

        // Array adapter( *activity*, *type of list view*, *my_array*)
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                tags_ex);

        //Set adapter
        lv.setAdapter(arrayAdapter);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //// Search View
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_spot, menu);

        //Set search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //// UP NAVIGATION - Action Bar
    ////////////////////////////////////////////////////////////////////////////////
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

/*
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
        final Post spot = new Post(ll);
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
            // or an error comment sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast comment if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG);
            }

        }

    }
*/
}