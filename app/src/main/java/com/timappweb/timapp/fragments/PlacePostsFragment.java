package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PostsAdapter;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit.client.Response;

public class PlacePostsFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    private Context         context;
    private PostsAdapter    postsAdapter;
    private ListView        lvTags;
    private View            progressView;
    private View            noPostsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_place_posts, container, false);

        //Initialize
        lvTags = (ListView) root.findViewById(R.id.list_people);
        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);

        initAdapter();
        loadPosts();

        return root;
    }

    private void initAdapter() {
        postsAdapter = new PostsAdapter(context);
        lvTags.setAdapter(postsAdapter);
    }

    private void loadPosts() {
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        RestClient.service().viewPostsForPlace(placeActivity.getPlaceId(), new RestCallback<List<Post>>(getContext()) {
            @Override
            public void success(List<Post> posts, Response response) {
                progressView.setVisibility(View.GONE);
                notifyPostsLoaded(posts);
            }
        });
    }

    private void notifyPostsLoaded(List<Post> posts) {
        //add tags to adapter
        for (Post post : posts) {
            postsAdapter.add(post);
        }
        if(posts.isEmpty()) {
            noPostsView.setVisibility(View.VISIBLE);
        }
    }

}
