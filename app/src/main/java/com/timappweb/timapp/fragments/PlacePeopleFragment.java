package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PostsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PlacePeopleFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    private Context         context;
    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    private PostsAdapter    postsAdapter;
    private ListView        lvTags;
    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private View            addButton;
    private TextView        tvAddButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_posts, container, false);

        initVariables(root);
        setListeners();
        initAdapter();
        loadPosts();

        placeActivity.notifyFragmentsLoaded();

        return root;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if(addButton!=null) {
                placeActivity.setPlusButtonVisibility(addButton.getVisibility()==View.VISIBLE);
            }
        }
    }

    private void initVariables(View root) {
        context= getActivity().getApplicationContext();
        placeActivity = (PlaceActivity) getActivity();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        //Views
        addButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        lvTags = (ListView) root.findViewById(R.id.list_people);
        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
    }

    private void setListeners() {
        addButton.setOnClickListener(placeActivity.getPeopleListener());
    }

    private void initAdapter() {
        postsAdapter = new PostsAdapter(context);
        lvTags.setAdapter(postsAdapter);
        postsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                String username = postsAdapter.getUsername(position);
                IntentsUtils.profile(placeActivity,username);
            }
        });
    }

    private void loadPosts() {
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse(Response<List<Post>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    progressView.setVisibility(View.GONE);
                    notifyPostsLoaded(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                progressView.setVisibility(View.GONE);
                noConnectionView.setVisibility(View.VISIBLE);
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

    public void setMainButtonVisibility(boolean bool) {
        if(bool) {
            addButton.setVisibility(View.VISIBLE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
    }

    public TextView getTvMainButton() {
        return tvAddButton;
    }

    public View getMainButton() {
        return addButton;
    }
}
