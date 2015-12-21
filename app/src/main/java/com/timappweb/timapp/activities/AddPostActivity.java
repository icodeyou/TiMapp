package com.timappweb.timapp.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.Managers.SearchAndSelectTagManager;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FilledTagsAdapter;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.TagsAdapter;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.fragments.AddPostMainFragment;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;


import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddPostActivity extends BaseActivity {

    private static final String     TAG = "AddSpot";

    // ---------------------------------------------------------------------------------------------

    private OnFragmentInteractionListener mListener;
    private String                      comment = null;
    private Boolean                     dummyLocation = true;
    private String                      mAddressOutput;

    // Views
    private TextView                    tvUserLocation;
    private ProgressBar                 progressBarLocation;
    private static ProgressDialog       progressDialog = null;
    private TextView                    mTvComment = null;
    private AddPostMainFragment         fragmentMain;
    private FragmentManager             fragmentManager =   getFragmentManager();

    // Location
    private MyLocationProvider          locationProvider;
    private LocationListener            mLocationListener;
    private AddressResultReceiver       mResultReceiver;        // For reverse geocoding
    private SearchAndSelectTagManager   searchAndSelectTagManager;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating add spot activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        this.initToolbar(true);

        // -----------------------------------------------------------------------------------------
        initLocationListener();
        initLocationProvider();
        initMainFragment();

        // -----------------------------------------------------------------------------------------
        // Init variables
        mResultReceiver = new AddressResultReceiver(new Handler());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        tvUserLocation = (TextView) findViewById(R.id.tv_user_location);
        progressBarLocation = (ProgressBar) findViewById(R.id.progress_bar_location);
        mTvComment = (TextView) findViewById(R.id.comment_textview);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void initMainFragment() {
        fragmentMain = new AddPostMainFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_add_spot, fragmentMain, "MainFragment");
        fragmentTransaction.commit();
    }

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

    public void onAddCommentClick(View view) {
        LayoutInflater inflater = getLayoutInflater();
        // Create
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Create EditText and place it in AlertDialog
        final EditText comment_dialog_text = new EditText(this);
        if (comment!=null) { comment_dialog_text.setText(comment); };
        builder.setView(comment_dialog_text);

        //Place cursor at the end of the EditText box
        comment_dialog_text.setSelection(comment_dialog_text.getText().length());

        //Set title and buttons
        builder.setTitle(R.string.add_comment_title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(comment_dialog_text.getWindowToken(), 0);
                        comment = comment_dialog_text.getText().toString();
                        //Set comment in the textview
                        TextView comment_textview = (TextView) findViewById(R.id.comment_textview);
                        Log.i(TAG, "new comment : " + comment);
                        comment_textview.setText(comment);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(comment_dialog_text.getWindowToken(), 0);
                    }
                });

        //Create Alertdialog
        AlertDialog comment_dialog = builder.create();

        //show Alertdialog
        comment_dialog.show();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


    }

    public AddPostMainFragment getFragmentMain() {
        return fragmentMain;
    }

    public FilledTagsAdapter getFilledTagsAdapter() {
        return (FilledTagsAdapter) fragmentMain.getSelectedTagsRV().getAdapter();
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

    public List<Tag> generateDummyData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("test", 0));
        data.add(new Tag("test2", 0));
        return data;
    }
    //Set onClickListener
    public void SubmitClickListener(View v) {
        //progressDialog.show();
        Log.d(TAG, "Clicked on submit spot");
        submitNewPost();
    }

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
        //post.tag_string = getTagsToString();
        post.comment = (String) mTvComment.getText();
        post.latitude = location.getLatitude();
        post.longitude = location.getLongitude();

        // Validating user input
        if (!post.validateForSubmit(mTvComment)){
            return;
        }
        Log.d(TAG, "Building spot: " + post);

        // Starting service
        this.progressDialog.setMessage(getResources().getString(R.string.please_wait));
        this.progressDialog.show();
        RestClient.service().addPost(post, new AddPostCallback(this, post));
    }

    protected void startIntentServiceReverseGeocoding(Location location) {
        Log.d(TAG, "Starting IntentService to get use address from location");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void displayAddressOutput() {
        progressBarLocation.setVisibility(View.INVISIBLE);
        tvUserLocation.setText(this.mAddressOutput);
        // TODO update size of tvUserLoaction to match parent size

        //tvUserLocation.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

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

        AddPostCallback(Context context,Post post){
            super(context);
            this.post = post;
        }

        @Override
        public void success(RestFeedback restFeedback, Response response) {
            progressDialog.hide();

            if (restFeedback.success && restFeedback.data.containsKey("id")){
                int id = Integer.valueOf(restFeedback.data.get("id"));
                Log.i(TAG, "Post has been saved. Id is : " + id);
                //Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot)
                IntentsUtils.post(this.context, id);
            }
            else{
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
}