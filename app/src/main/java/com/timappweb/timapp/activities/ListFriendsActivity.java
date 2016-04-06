package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ListFriendsActivity extends BaseActivity{

    private String TAG = "ListFriendsActivity";
    private List<User> allFbFriends;
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    //private SimpleFacebook mSimpleFacebook;
    private View noFriendsView;

    private OnFriendsListener onFriendsListener;
    private View progressView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_view);
        progressView = findViewById(R.id.loading_friends);

        initAdapterListFriends();
        loadFriends();
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new FriendsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                onItemListClicked(position);
            }
        });
    }

    private void loadFriends(){
        Call<PaginationResponse<User>> call = RestClient.service().friends();
        apiCalls.add(call);
        call.enqueue(new RestCallback<PaginationResponse<User>>(this) {
            @Override
            public void onResponse200(Response<PaginationResponse<User>> response) {
                onUserLoaded(response.body().items);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                progressView.setVisibility(View.GONE);
            }
        });
    }

    private void onUserLoaded(List<User> items){

        allFbFriends = items;
        if(allFbFriends.size()==0) {
            noFriendsView.setVisibility(View.VISIBLE);
        } else {
            noFriendsView.setVisibility(View.GONE);
            adapter.setData(items);
        }
        progressView.setVisibility(View.GONE);
    }

    private void onItemListClicked(int position) {
        User friend = allFbFriends.get(position);
        IntentsUtils.profile(this, friend);
    }
}
