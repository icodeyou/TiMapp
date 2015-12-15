package com.timappweb.timapp.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
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
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.SearchHistory;
import com.timappweb.timapp.utils.Util;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddSpotActivity extends BaseActivity {

    private static final String     TAG = "AddSpot";

    // ---------------------------------------------------------------------------------------------

    private OnFragmentInteractionListener mListener;
    private String                      comment = null;
    private Boolean                     dummyLocation = true;
    private String                      mAddressOutput;
    SearchHistory<Tag>                  searchHistory;

    // Views
    private TextView                    tvUserLocation;
    private ProgressBar                 progressBarLocation;
    private static ProgressDialog       progressDialog = null;
    private SearchView                  searchView = null;
    private RecyclerView                rv_suggestedTags = null;
    private TextView                    mTvComment = null;

    // Location
    private MyLocationProvider          locationProvider;
    private LocationListener            mLocationListener;
    private AddressResultReceiver       mResultReceiver;        // For reverse geocoding

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating add spot activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        this.initToolbar(true);

        // -----------------------------------------------------------------------------------------
        // Init variables
        searchHistory = new SearchHistory<>();
        mResultReceiver = new AddressResultReceiver(new Handler());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        tvUserLocation = (TextView) findViewById(R.id.tv_user_location);
        progressBarLocation = (ProgressBar) findViewById(R.id.progress_bar_location);
        mTvComment = (TextView) findViewById(R.id.comment_textview);

        // -----------------------------------------------------------------------------------------
        // Init tags recycler view
        iniTagRecyclerView();

        initLocationListener();
        initLocationProvider();
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

    private void iniTagRecyclerView() {
        final RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_addSpot);
        final SelectedTagsAdapter selectedTagsAdapter = new SelectedTagsAdapter(this, new LinkedList<Tag>());
        rv_savedTagsList.setAdapter(selectedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_savedTagsList.setLayoutManager(manager);

        //Scroll untill the end of the RecyclerView, so that we can see the last tag.
        rv_savedTagsList.scrollToPosition(selectedTagsAdapter.getItemCount() - 1);

        //////////////////Import examples into the vertical ListView////////////////////
        //get RecyclerView from XML
        rv_suggestedTags = (RecyclerView) findViewById(R.id.suggested_tags_filter);

        // set Adapter
        SuggestedTagsAdapter suggestedTagsAdapter = new SuggestedTagsAdapter(this, null);
        rv_suggestedTags.setAdapter(suggestedTagsAdapter);
        this.suggestTag("");

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

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        SearchView.OnQueryTextListener queryTextListener = new OnQueryTextListener();
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

    /**
     * Suggest tag according to user input
     * Cache results according to the term given
     * @param term
     */
    public void suggestTag(final String term){
        searchHistory.setLastSearch(term);
        final SuggestedTagsAdapter adapter = (SuggestedTagsAdapter) rv_suggestedTags.getAdapter();
        // Data are in cache
        if (searchHistory.hasTerm(term)){
            adapter.setData(searchHistory.get(term).getData());
        }
        else {
            // Data are not in cache, try searching for a sub term
            SearchHistory.Item subHistory = searchHistory.get(term);
            if (subHistory != null){
                adapter.setData(subHistory.getData());
                if (subHistory.isComplete()){
                    return ;
                }
            }
            searchHistory.create(term);

            RestClient.service().suggest(term, new RestCallback<List<Tag>>(this) {
                @Override
                public void success(List<Tag> tags, Response response) {
                    Log.d(TAG, "Got suggested tags from server with term " + term + "* : " + tags.size());
                    searchHistory.set(term, tags);
                    if (searchHistory.isLastSearch(term)){
                        Log.d(TAG, "'" + term + "' is the last search, setting data");
                        adapter.setData(tags);
                    }
                }

            });
        }
    }
    public List<Tag> generateDummyData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("test", 0));
        data.add(new Tag("test2", 0));
        return data;
    }
    public List<Tag> addDataToAdapter(String newData, SelectedTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

    //Set onClickListener
    public void SubmitClickListener(View v) {
        //progressDialog.show();
        Log.d(TAG, "Clicked on submit spot");
        submitNewPost();
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
            displayAddressOutput();

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


    private class OnQueryTextListener implements SearchView.OnQueryTextListener {

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

            searchView.setIconified(true);

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.length() >= 2){
                suggestTag(newText);
            }
            return false;
        }
    }
}