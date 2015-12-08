package com.timappweb.timapp.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.BoringLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectedTagsAdapter;
import com.timappweb.timapp.adapters.SuggestedTagsAdapter;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.listeners.MyLinearLayoutManager;
import com.timappweb.timapp.listeners.RecyclerItemClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.Feedback;
import com.timappweb.timapp.utils.MyLocationListener;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;



import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddSpotActivity extends BaseActivity {

    private static final String TAG = "AddSpot";
    private OnFragmentInteractionListener mListener;
    private String comment = null;
    ProgressDialog progressDialog = null;
    AlertDialog alertDialog = null;
    private Boolean dummyLocation = true;

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

        mResultReceiver = new AddressResultReceiver(new Handler());
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage(getResources().getString(R.string.please_wait));


        this.alertDialog = (new AlertDialog.Builder(this)).create();

        this.locationProvider = new MyLocationProvider(this);
        this.locationProvider.setLocationListener(mLocationListener);
        this.locationProvider.getUserLocation();

        Log.d(TAG, "Location provider has been set");

        /////////////////Submit button clicked //////////////////////////////////////
        Button b = (Button) findViewById(R.id.button_submit_spot);
        b.setOnClickListener(new SubmitClickListener());

        /////////////////Saved tags Recycler view//////////////////////////////////////
        // Get recycler view
        final RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_addSpot);
        final SelectedTagsAdapter selectedTagsAdapter = new SelectedTagsAdapter(this, generateData());
        rv_savedTagsList.setAdapter(selectedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_savedTagsList.setLayoutManager(manager);

        //Scroll untill the end of the RecyclerView, so that we can see the last tag.
        rv_savedTagsList.scrollToPosition(selectedTagsAdapter.getItemCount() - 1);

        //////////////////Import examples into the vertical ListView////////////////////
        //get RecyclerView from XML
        RecyclerView rv_suggestedTags = (RecyclerView) findViewById(R.id.suggested_tags_filter);

        // set Adapter
        SuggestedTagsAdapter suggestedTagsAdapter = new SuggestedTagsAdapter(this, generateData());
        rv_suggestedTags.setAdapter(suggestedTagsAdapter);

        //Set LayoutManager
        MyLinearLayoutManager manager_suggestedTags = new MyLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        rv_suggestedTags.setLayoutManager(manager_suggestedTags);

        //set onClickListener
        rv_suggestedTags.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                Log.d(TAG, "Item is touched !");
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                SuggestedTagsAdapter STadapter = (SuggestedTagsAdapter) adapter;
                String selectedTag = STadapter.getData().get(position).getName();
                addDataToAdapter(selectedTag, selectedTagsAdapter);
                //Scroll untill the end of the RecyclerView, so that we can see the last tag.
                rv_savedTagsList.scrollToPosition(selectedTagsAdapter.getItemCount() - 1);
            }
        }));
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

        final SearchView finalSearchView = searchView;
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get recycler view
                RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_addSpot);
                //Get adapter
                RecyclerView.Adapter adapter = rv_savedTagsList.getAdapter();
                SelectedTagsAdapter selectedTagsAdapter = (SelectedTagsAdapter) adapter;
                //Set new values
                addDataToAdapter(query, selectedTagsAdapter);
                //Scroll untill the end of the RecyclerView, so that we can see the last tag.
                rv_savedTagsList.scrollToPosition(selectedTagsAdapter.getItemCount() - 1);

                finalSearchView.setIconified(true);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Get the text each time the value is change in the searchbox
                return false;
            }
        };

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(queryTextListener);
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

    /////////GENERATE DATA/////////////////////
    public List<Tag> generateData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("bar", 0));
        data.add(new Tag("carrote", 0));
        data.add(new Tag("paindepice", 0));
        data.add(new Tag("ohjoie", 0));
        data.add(new Tag("jaimelavie", 0));
        data.add(new Tag("etsionfaisaitdespates", 0));
        data.add(new Tag("nonmaisgrave", 0));
        data.add(new Tag("aplusmarcel", 0));
        data.add(new Tag("persojaipastresfaim", 0));
        data.add(new Tag("barbecueparty", 0));
        data.add(new Tag("BBQ", 0));
        data.add(new Tag("ILoveYou", 0));
        return data;
    }
    public List<Tag> addDataToAdapter(String newData, SelectedTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

    private class SubmitClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //progressDialog.show();
            Log.d(TAG, "Clicked on submit spot");
            submitNewPost();
        }
    }

    private String getTagsToString(){
        RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_addSpot);
        SelectedTagsAdapter adapter = (SelectedTagsAdapter) rv_savedTagsList.getAdapter();
        String inputTags = "";
        List<Tag> selectedTags = adapter.getData();

        for (Tag tag: selectedTags){
            inputTags += tag.name + ",";
        }
        return inputTags;
    }

    public void submitNewPost(){
        // 1) Get the user position
        this.progressDialog.setMessage(getResources().getString(R.string.please_wait));
        this.progressDialog.show();
        Location location = null;

        try{
            location = this.locationProvider.getLastLocation();
        }
        catch (NoLastLocationException ex) {

            if (!BuildConfig.DEBUG) {
                Log.i(TAG, "Cannot get position.");
                //this.alertDialog.setMessage(getResources().getString(R.string.error_cannot_get_location));
                //this.alertDialog.show();
                this.progressDialog.setMessage(getResources().getString(R.string.waiting_for_location));
                this.progressDialog.show();
                return;
            }
            else if (dummyLocation){
                Log.i(TAG, "Debug mode. Using mock position.");
                String providerName = "";
                location = new Location(providerName);
                location.setLatitude(10);
                location.setLongitude(10);
                location.setAltitude(0);
                location.setTime(System.currentTimeMillis());
            }
        }


        //Location location = locationProvider.getLastLocation();
        // if precision sucks..
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "User position is: " + userLatLng + " with an accuracy of " + location.getAccuracy());
        // 3) Call the service to add the spot
        // - Build the spot
        final Post post = new Post(userLatLng);
        post.tag_string = getTagsToString();
        post.comment = (String) ((TextView) findViewById(R.id.comment_textview)).getText();
        post.latitude = location.getLatitude();
        post.longitude = location.getLongitude();

        Log.d(TAG, "Building spot: " + post);
        RestClient.service().addSpot(post, new AddPostCallback());
    }

    private final LocationListener mLocationListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.i(TAG, "User location has changed: " + Util.print(location));
            progressDialog.hide();
            startIntentService(location);
        }
    };
    private MyLocationProvider locationProvider;

    private AddressResultReceiver mResultReceiver;

    protected void startIntentService(Location location) {
        Log.d(TAG, "Starting IntentService to get use address from location");
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

    //////////////////////////////////////////////////////////////à supprimer??
    /*
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

    */


    private class AddPostCallback extends RestCallback<RestFeedback> {

        @Override
        public void success(RestFeedback restFeedback, Response response) {
            progressDialog.hide();

            if (restFeedback.success){
                Log.i(TAG, "Post has been saved!");
                Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot);
                // TODO change view
                Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
                startActivity(intent);
            }
            else{
                Log.i(TAG, "Cannot add spot: " + response.getReason() + " - " + restFeedback.toString());
                alertDialog.setMessage(getResources().getString(R.string.error_webservice_connection));
                alertDialog.show();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressDialog.hide();
            alertDialog.setMessage(getResources().getString(R.string.error_webservice_connection));
            alertDialog.show();
        }

    }

}