package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.entities.PlacesInvitation;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class InvitationsActivity extends BaseActivity{

    private String TAG = "ListFriendsActivity";
    private List<PlacesInvitation> invitations;
    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;

    private View progressView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        progressView = findViewById(R.id.loading_invitations);

        initAdapterListFriends();
        loadInvitations();
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(llm);

        adapter = new InvitationsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                onItemListClicked(position);
            }
        });
    }

    private void loadInvitations(){

        Call<PaginationResponse<PlacesInvitation>> call = RestClient.service().inviteReceived();
        call.enqueue(new RestCallback<PaginationResponse<PlacesInvitation>>(this) {
            @Override
            public void onResponse200(Response<PaginationResponse<PlacesInvitation>> response) {
                onInvitationsLoaded(response.body().items);
            }

            @Override
            public void onFinish() {
                progressView.setVisibility(View.GONE);
            }
        });

    }

    private void onInvitationsLoaded(List<PlacesInvitation> items){

        invitations = items;
        if(invitations.size()==0) {
            noInvitationsView.setVisibility(View.VISIBLE);
        } else {
            noInvitationsView.setVisibility(View.GONE);
            adapter.setData(items);
        }
        progressView.setVisibility(View.GONE);
    }

    private void onItemListClicked(int position) {
        PlacesInvitation invitation = invitations.get(position);
        IntentsUtils.viewSpecifiedPlace(this, invitation.place);
    }
}
