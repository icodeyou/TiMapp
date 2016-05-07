package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.ColorPublishButtonRadiusOnTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.EventView;

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
    private EventView eventView;
    private Button confirmButton;
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
        eventView = (EventView) findViewById(R.id.event_view);
        progressView = findViewById(R.id.progress_view);
        confirmButton = (Button) findViewById(R.id.confirm_button);
        //textButton1 = (TextView) findViewById(R.id.text_confirm_button1);
        //textButton2 = (TextView) findViewById(R.id.text_confirm_button2);

        initButtonAnimation();
        initPlaceView();
        initAdapters();
        initClickSelectedTag();
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

    @Override
    public void onBackPressed() {
        IntentsUtils.addPostStepTags(this, currentPlace, currentPost);
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    private void initButtonAnimation() {
        AlphaAnimation anim = new AlphaAnimation(0, 1);
        anim.setDuration(1000);
        confirmButton.startAnimation(anim);
    }

    private void initPlaceView() {
        eventView.setEvent(currentPlace);
    }

    private void initAdapters() {
        HorizontalTagsAdapter selectedTagsAdapter = selectedTagsRV.getAdapter();
        selectedTagsAdapter.setData(currentPost.getTags());
    }

    private void initClickSelectedTag() {
        final Activity activity = this;
        selectedTagsRV.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item at position : " + position);
                List<Tag> tagList = currentPost.getTags();
                tagList.remove(position);
                currentPost.setTags(tagList);
                IntentsUtils.addPostStepTags(activity, currentPlace, currentPost);
            }
        });
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

        /*confirmButton.setOnTouchListener(
                new ColorPublishButtonRadiusOnTouchListener(this, textButton1, textButton2));*/

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressView(true);
                currentPost.place_id = currentPlace.id;
                // Validating user input
                if (!currentPost.validateForSubmit()) {
                    Toast.makeText(activity, "Invalid inputs", Toast.LENGTH_LONG).show(); // TODO proper message
                    return;
                }
                Log.d(TAG, "Submitting post: " + currentPost);

                Call<RestFeedback> call = RestClient.service().addPost(currentPost);
                call.enqueue(new AddPostCallback(activity, currentPost, currentPlace));
            }
        });
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.GONE);
            confirmButton.clearAnimation();
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
                QuotaManager.instance().add(QuotaType.PLACES);
            }
            //QuotaManager.instance().add(QuotaType.ActionTypeName.CREATE_PLACE);
            //CacheData.setLastPost(post);
            QuotaManager.instance().add(QuotaType.ADD_POST);

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
