package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PostsAdapter;
import com.timappweb.timapp.entities.Post;

import java.util.ArrayList;

public class PlacePostsActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_comment);

        this.initToolbar(true);

        //Find listview in XML
        ListView lvTags = (ListView) findViewById(R.id.list_posts);
        // pass context and data to the custom adapter
        PostsAdapter postsAdapter = new PostsAdapter(this,generateData());
        //Set adapter
        lvTags.setAdapter(postsAdapter);
    }

    //Generate Data for ListView
    private ArrayList<Post> generateData(){
        ArrayList<Post> posts = new ArrayList<>();
        posts.add(Post.createDummy());
        posts.add(Post.createDummy());
        return posts;
    }
}
