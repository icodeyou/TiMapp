package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.ColorPublishButtonRadiusOnTouchListener;
import com.timappweb.timapp.rest.PostAndPlaceRequest;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

import retrofit2.Call;


public class PublishActivity extends BaseActivity{

    private String TAG = "PublishActivity";
    private View progressView;
    private Activity activity;

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private Place currentPlace = null;
    private Post currentPost = null;
    private CheckBox checkBox = null;
    private ListView placeListView;
    private LinearLayout confirmButton;
    private TextView textButton1;
    private TextView textButton2;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        // Check that we gave the place as an extra parameter
        this.currentPlace = IntentsUtils.extractPlace(getIntent());
        this.currentPost = IntentsUtils.extractPost(getIntent());
        if (this.currentPlace == null || this.currentPost == null){
            Log.d(TAG, "Place is null");
            IntentsUtils.addPostStepLocate(this);
            return;
        }

        setContentView(R.layout.activity_publish);
        this.initToolbar(false);

        //Initialize variables
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        selectedTagsRV = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);
        placeListView = (ListView) findViewById(R.id.place_lv);
        progressView = findViewById(R.id.progress_view);
        confirmButton = (LinearLayout) findViewById(R.id.confirm_button);
        textButton1 = (TextView) findViewById(R.id.text_confirm_button1);
        textButton2 = (TextView) findViewById(R.id.text_confirm_button2);

        initAdapters();
        setListeners();
        setCheckbox();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                IntentsUtils.addPostStepTags(this, currentPlace, currentPost);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    private void initAdapters() {
        PlacesAdapter placesAdapter = new PlacesAdapter(this);
        placesAdapter.add(currentPlace);
        placeListView.setAdapter(placesAdapter);

        HorizontalTagsAdapter selectedTagsAdapter = selectedTagsRV.getAdapter();
        selectedTagsAdapter.setData(currentPost.getTags());
    }

    private void setCheckbox() {
        currentPost.anonymous = checkBox.isSelected();
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Is anonymous: " + checkBox.isSelected());
                currentPost.anonymous = checkBox.isSelected();
            }
        });
    }

    public void setListeners() {

        confirmButton.setOnTouchListener(
                new ColorPublishButtonRadiusOnTouchListener(this, textButton1, textButton2));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressView(true);
                // Validating user input
                if (!currentPost.validateForSubmit()) {
                    Toast.makeText(activity, "Invalid inputs", Toast.LENGTH_LONG).show(); // TODO proper message
                    return;
                }
                Log.d(TAG, "Submitting post: " + currentPost);

                Call<RestFeedback> call = null;
                if (currentPlace.isNew()){
                    call = RestClient.service().addPost(new PostAndPlaceRequest(currentPost, currentPlace));
                }
                else{
                    currentPost.place_id = currentPlace.id;
                    call = RestClient.service().addPost(currentPost);
                }
                call.enqueue(new AddPostCallback(activity, currentPost, currentPlace));
            }
        });
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.GONE);
            selectedTagsRV.setVisibility(View.GONE);
        }
        else {
            progressView.setVisibility(View.GONE);
            confirmButton.setVisibility(View.VISIBLE);
            selectedTagsRV.setVisibility(View.VISIBLE);
        }
    }

    //----------------------------------------------------------------------------------------------
    //Inner classes
    private class AddPostCallback extends RestFeedbackCallback {

        private Post post;
        private Place place;
        // TODO get post from server instead. (in case tags are in a black list)

        AddPostCallback(Context context, Post post, Place place) {
            super(context);
            this.post = post;
            this.place = place;
        }

        @Override
        public void onActionSuccess(RestFeedback feedback) {
            int id = Integer.valueOf(feedback.data.get("id"));
            int placeId = Integer.valueOf(feedback.data.get("place_id"));
            Log.i(TAG, "Post for place " + placeId + " has been saved with id: " + id);

            // Caching data
            post.place_id = placeId;
            post.created = Util.getCurrentTimeSec();
            if (place.isNew()){
                place.id = placeId;
                place.created = Util.getCurrentTimeSec();
                CacheData.setLastPlace(place);
            }
            CacheData.setLastPost(post);

            IntentsUtils.viewPlaceFromPublish(activity, placeId);
        }

        @Override
        public void onActionFail(RestFeedback feedback) {
            Toast.makeText(this.context, feedback.message, Toast.LENGTH_LONG).show();
            setProgressView(false);
        }


        @Override
        public void onFailure(Throwable t) {
            super.onFailure(t);
            setProgressView(false);
        }
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

}
