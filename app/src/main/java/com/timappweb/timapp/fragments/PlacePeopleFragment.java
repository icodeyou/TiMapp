package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PlaceUsersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.entities.UsersPlace;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.QueryMap;


public class PlacePeopleFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    private Context         context;
    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    private PlaceUsersAdapter placeUsersAdapter;
    private RecyclerView peopleRv;
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
        initRv();
        initAdapter();
        loadPosts();
        loadByStatus(null);

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
        placeActivity = (PlaceActivity) getActivity();
        context= placeActivity.getApplicationContext();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        //Views
        addButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        peopleRv = (RecyclerView) root.findViewById(R.id.list_people);
        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
    }

    private void setListeners() {
        addButton.setOnClickListener(placeActivity.getPeopleListener());
    }

    private void initRv() {
        peopleRv.setHasFixedSize(true);
        peopleRv.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initAdapter() {
        placeUsersAdapter = new PlaceUsersAdapter(context);
        peopleRv.setAdapter(placeUsersAdapter);
        placeUsersAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                User user = placeUsersAdapter.getPost(position).getUser();
                Log.d(TAG, "Viewing profile user: " + user);
                IntentsUtils.profile(placeActivity, user);
            }
        });
    }

    private void loadPosts() {
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse(Response<List<Post>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    List<Post> paginateData = response.body();
                    progressView.setVisibility(View.GONE);
                    notifyPostsLoaded(paginateData);
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

    private void loadByStatus(List<UserPlaceStatus> status){
        Map<String, String> conditions = new HashMap<>();
        //conditions.put("status", String.valueOf(status));

        Call<PaginationResponse<UsersPlace>> call = RestClient.service().viewUsersForPlace(placeActivity.getPlaceId(), conditions);
        call.enqueue(new RestCallback<PaginationResponse<UsersPlace>>(getContext()) {
            @Override
            public void onResponse(Response<PaginationResponse<UsersPlace>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    PaginationResponse<UsersPlace> paginateData = response.body();
                    progressView.setVisibility(View.GONE);
                    notifyUsersStatusLoaded(paginateData.items);
                }
            }
        });
    }

    private void notifyPostsLoaded(List<Post> items) {
        if(items.isEmpty()) {
            noPostsView.setVisibility(View.VISIBLE);
            return;
        }

        List<PlaceUserInterface> interfaceList = new ArrayList<>(items.size());
        for (PlaceUserInterface post : items) {
            interfaceList.add(post);
        }
        placeUsersAdapter.setData(interfaceList);
    }
    private void notifyUsersStatusLoaded(List<UsersPlace> users) {
        // TODO jean
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
