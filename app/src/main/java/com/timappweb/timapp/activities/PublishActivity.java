package com.timappweb.timapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class PublishActivity extends BaseActivity{

    private String TAG = "PublishActivity";
    private static ProgressDialog progressDialog = null;

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private Place currentPlace = null;
    private Post currentPost = null;
    public Location currentLocation = null;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that we gave the place as an extra parameter
        this.currentPlace = IntentsUtils.extractPlace(getIntent());
        this.currentPost = IntentsUtils.extractPost(getIntent());
        if (this.currentPlace == null || this.currentPost == null){
            Log.d(TAG, "Place is null");
            IntentsUtils.addPost(this);
            return;
        }

        setContentView(R.layout.activity_publish);
        this.initToolbar(true);

        //Initialize variables
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        LinearLayout layout_checkbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        selectedTagsRV = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);

        //set saved tags in selectedTagsRV
        HorizontalTagsAdapter selectedTagsAdapter = selectedTagsRV.getAdapter();
        selectedTagsAdapter.setData(currentPost.getTags());

        layout_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.toggle();
            }
        });

        this.initLocationProvider(new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    public void submitNewPost(View view){
        this.currentPost.latitude = currentLocation.getLatitude();
        this.currentPost.longitude = currentLocation.getLongitude();

        // Validating user input
        if (!currentPost.validateForSubmit()){
            Toast.makeText(this, "Invalid inputs", Toast.LENGTH_LONG); // TODO proper message
            return;
        }
        Log.d(TAG, "Submitting post: " + currentPost);

        // Starting service
        //this.progressDialog.setMessage(getResources().getString(R.string.please_wait));
        //this.progressDialog.show();
        RestClient.service().addPost(this.currentPost, new AddPostCallback(this, this.currentPost));
    }

    //----------------------------------------------------------------------------------------------
    //Inner classes
    private class AddPostCallback extends RestCallback<RestFeedback> {

        private final Post post;
        // TODO get post from server instead. (in case tags are in a black list)

        AddPostCallback(Context context, Post post) {
            super(context);
            this.post = post;
        }

        @Override
        public void success(RestFeedback restFeedback, Response response) {
            //progressDialog.hide();

            if (restFeedback.success && restFeedback.data.containsKey("id")) {
                int id = Integer.valueOf(restFeedback.data.get("id"));
                Log.i(TAG, "Post has been saved. Id is : " + id);
                //Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot)
                IntentsUtils.viewPost(this.context, id);
            } else {
                Log.i(TAG, "Cannot add post: " + response.getReason() + " - " + restFeedback.toString());
                Toast.makeText(this.context, restFeedback.message, Toast.LENGTH_LONG);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            //progressDialog.hide();
            Toast.makeText(this.context, R.string.error_webservice_connection, Toast.LENGTH_LONG);
        }
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS
    public String getTagsToString(){
        HorizontalTagsAdapter adapter = (HorizontalTagsAdapter) selectedTagsRV.getAdapter();
        String inputTags = "";
        List<Tag> selectedTags = adapter.getData();

        for (Tag tag: selectedTags){
            inputTags += tag.name + ",";
        }
        return inputTags;
    }

}
