package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;


public class PostActivity extends BaseActivity {

    private static final String TAG = "PostActivity" ;
    private EventPost currentEventPost = null;
    private ArrayAdapter<String> tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //------------------------------------------------------------------------------------------
        ListView listViewTags = (ListView) findViewById(R.id.tags_lv);
        tagsAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        listViewTags.setAdapter(tagsAdapter);
        //------------------------------------------------------------------------------------------
        // 2 cases:
        //  - we gave the full eventPost => we just display data
        //  - we gave the eventPost remote_id => we need to request the server to have extra informations
        currentEventPost = (EventPost) getIntent().getSerializableExtra("eventPost");
        int postId = (int) getIntent().getExtras().getInt("eventPost.id", -1);

        if (currentEventPost == null && postId <= 0){
            Log.e(TAG, "The eventPost is null");
            IntentsUtils.home(this);
            return;
        }
        else if (postId > 0){
            Log.d(TAG, "Loading eventPost from eventPost id " + postId);
            this.loadPost(postId);
        }
        else {
            Log.d(TAG, "Using eventPost given in extra " + currentEventPost);
            this.fetchDataToView();
            if (!currentEventPost.hasTags()){
                this.loadTagsForPost();
            }
        }
        //------------------------------------------------------------------------------------------

        this.initToolbar(true);
    }

    private void loadTagsForPost() {
        Call<List<Tag>> call = RestClient.service().loadTagsFromPost(currentEventPost.getMarkerId());
        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<Tag>>() {
                    @Override
                    public void successful(List<Tag> tags) {
                        currentEventPost.setTags(tags);
                        fetchDataToView();
                    }
                })
                .perform();
    }

    private void loadPost(int postId) {
        Call<EventPost> call = RestClient.service().viewPost(currentEventPost.getMarkerId());
        RestClient.buildCall(call)
                .onResponse(new HttpCallback<EventPost>() {
                    @Override
                    public void successful(EventPost eventPost) {
                        currentEventPost = eventPost;
                        fetchDataToView();
                    }
                })
                .perform();
    }

    private void fetchDataToView(){
        TextView textViewCreated = (TextView) findViewById(R.id.post_created);
        TextView textViewUsername = (TextView) findViewById(R.id.post_username);
        TextView textViewPostName = (TextView) findViewById(R.id.post_name);

        textViewCreated.setText(currentEventPost.getPrettyTimeCreated());
        textViewUsername.setText(currentEventPost.getUsername());
        textViewPostName.setText(currentEventPost.getAddress());

        if (currentEventPost.hasTags()){
            tagsAdapter.addAll(currentEventPost.getTagsToStringArray());
            tagsAdapter.notifyDataSetChanged();
        }

    }

    public void onProfileCLick(View view) {
        Log.d(TAG, "PostActivity.onProfileCLick()");
        if (currentEventPost.user != null){
            Log.d(TAG, "Viewing profile user: " + currentEventPost.user);
            IntentsUtils.profile(this, currentEventPost.user);
        }
    }
}
