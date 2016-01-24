package com.timappweb.timapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class PublishActivity extends BaseActivity{

    private String TAG = "PublishActivity";
    private View progressView;
    private Context context;

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private Place currentPlace = null;
    private Post currentPost = null;
    private CheckBox checkBox = null;
    private ListView placeListView;
    private Button confirmButton;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

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
        confirmButton = (Button) findViewById(R.id.confirm_button);

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
                currentPost.anonymous = v.isSelected();
            }
        });
    }

    public void setListeners() {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressView(true);
                // Validating user input
                if (!currentPost.validateForSubmit()) {
                    Toast.makeText(context, "Invalid inputs", Toast.LENGTH_LONG); // TODO proper message
                    return;
                }
                Log.d(TAG, "Submitting post: " + currentPost);

                // Starting service
                //this.progressDialog.setMessage(getResources().getString(R.string.please_wait));
                //this.progressDialog.show();
                RestClient.service().addPost(currentPost, new AddPostCallback(context, currentPost));
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
    private class AddPostCallback extends RestCallback<RestFeedback> {

        private final Post post;
        // TODO get post from server instead. (in case tags are in a black list)

        AddPostCallback(Context context, Post post) {
            super(context);
            this.post = post;
        }

        @Override
        public void success(RestFeedback restFeedback, Response response) {
            if (restFeedback.success && restFeedback.data.containsKey("id")) {
                int id = Integer.valueOf(restFeedback.data.get("id"));
                Log.i(TAG, "Post has been saved. Id is : " + id);
                //Feedback.show(getApplicationContext(), R.string.feedback_webservice_add_spot)
                IntentsUtils.viewPlaceFromPublish(this.context, post.place_id);
            } else {
                Log.i(TAG, "Cannot add post: " + response.getReason() + " - " + restFeedback.toString());
                Toast.makeText(this.context, restFeedback.message, Toast.LENGTH_LONG);
                setProgressView(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            super.failure(error);
            progressView.setVisibility(View.GONE);
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
