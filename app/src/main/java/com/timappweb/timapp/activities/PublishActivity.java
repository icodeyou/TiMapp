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

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.TagsAdapter;
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

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        this.initToolbar(true);

        //Initialize variables
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
        LinearLayout layout_checkbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        selectedTagsRV = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);

        //init adapter
        TagsAdapter selectedTagsAdapter = selectedTagsRV.getAdapter();

        //Get Extra
        Intent intent = getIntent();
        ArrayList<String> finalTagsString = intent.getStringArrayListExtra("finalTags");
        ArrayList<Tag> finalTags = selectedTagsAdapter.getTagsFromStrings(finalTagsString);
        selectedTagsAdapter.setData(finalTags);

        layout_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.toggle();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    private void submitNewPost(){

        //Create dummy name
        Location location = null;
        Log.i(TAG, "Debug mode. Using mock position.");
        String providerName = "";
        location = new Location(providerName);
        location.setLatitude(10);
        location.setLongitude(10);
        location.setAltitude(0);
        location.setTime(System.currentTimeMillis());

        // if precision sucks..
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "User position is: " + userLatLng + " with an accuracy of " + location.getAccuracy());

        // Call the service to add the spot
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

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
