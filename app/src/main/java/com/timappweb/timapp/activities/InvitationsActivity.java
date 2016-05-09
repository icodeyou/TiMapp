package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.activeandroid.query.From;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.PlacesInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

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

        getSupportLoaderManager().initLoader(0, null, new InvitationLoader());
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
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


    private void updateView(List<PlacesInvitation> items){

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
        IntentsUtils.viewSpecifiedEvent(this, invitation.place);
    }



    // =============================================================================================

    class InvitationLoader implements LoaderManager.LoaderCallbacks<List<PlacesInvitation>>{

        @Override
        public Loader<List<PlacesInvitation>> onCreateLoader(int id, Bundle args) {
            From query = MyApplication.getCurrentUser().getInviteReceivedQuery();
            SyncBaseModel.getRemoteEntries(InvitationsActivity.this, query, DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED, 300 * 1000);
            //if (invites != null){
            //    updateView(invites);
            //}
            return new ModelLoader<>(InvitationsActivity.this, PlacesInvitation.class, query, false);
        }

        @Override
        public void onLoadFinished(Loader<List<PlacesInvitation>> loader, List<PlacesInvitation> data) {
            Log.d(TAG, "Place loaded finish");
            updateView(data);
        }

        @Override
        public void onLoaderReset(Loader<List<PlacesInvitation>> loader) {

        }
    }
}
