package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.EventUsersHeaderAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.List;

import retrofit2.Call;


public class EventPeopleFragment extends EventBaseFragment {

    private static final String TAG = "EventTagsFragment";
    private static final long MAX_UPDATE_DELAY = 3600 * 1000;

    private Context         context;

    private EventUsersHeaderAdapter placeUsersAdapter;

    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton postButton;
    private RefreshableRecyclerView mRecyclerView;
    private RecyclerViewMaterialAdapter mAdapter;
    //private ObservableScrollView viewContainer;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_people, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context= eventActivity.getBaseContext();

        // viewContainer = (ObservableScrollView) root.findViewById(R.id.scrollView);
        progressView = view.findViewById(R.id.progress_view);
        noPostsView = view.findViewById(R.id.no_posts_view);
        noConnectionView = view.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RefreshableRecyclerView) view.findViewById(R.id.list_people);
        postButton = (FloatingActionButton) view.findViewById(R.id.post_button);

        initRecyclerView();

        getLoaderManager().initLoader(EventActivity.LOADER_ID_USERS, null, new UserStatusLoader(this.getContext(), eventActivity.getEvent()));

        if (MyApplication.isLoggedIn()){
            getLoaderManager().initLoader(EventActivity.LOADER_ID_INVITATIONS, null, new InviteSentLoader(this.getContext(), eventActivity.getEvent()));
        }

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().getLoader(EventActivity.LOADER_ID_USERS).forceLoad();
                if (MyApplication.isLoggedIn()) {
                    getLoaderManager().getLoader(EventActivity.LOADER_ID_INVITATIONS).forceLoad();
                }
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), eventActivity.getEvent(), IntentsUtils.ACTION_PEOPLE);
            }
        });

    }

    private void initRecyclerView() {

        //Construct Adapter
        placeUsersAdapter = new EventUsersHeaderAdapter(context);
        placeUsersAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.v(TAG, "Accessing position: " + position);
                PlaceUserInterface user = placeUsersAdapter.getData(position);
                Log.d(TAG, "Viewing profile user: " + user.getUser());
                IntentsUtils.profile(eventActivity, user.getUser());
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(placeUsersAdapter));

        mAdapter = new RecyclerViewMaterialAdapter(placeUsersAdapter);
        mRecyclerView.setAdapter(mAdapter);


        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }


    private void loadPosts() {
        Call<List<EventPost>> call = RestClient.service().viewPostsForPlace(eventActivity.getEventId());
        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<EventPost>>() {
                    @Override
                    public void successful(List<EventPost> list) {
                        placeUsersAdapter.addData(UserPlaceStatusEnum.HERE, list);
                    }

                    @Override
                    public void notSuccessful() {
                        noConnectionView.setVisibility(View.VISIBLE);
                    }
                })
                .perform();
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }


    // =============================================================================================

    class UserStatusLoader extends MultipleEntryLoaderCallback<UserEvent> {

        public UserStatusLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY, DataSyncAdapter.SYNC_TYPE_EVENT_USERS, UserEvent.queryForPlace(event));
            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout, false);
        }

        @Override
        public void onLoadFinished(Loader<List<UserEvent>> loader, List<UserEvent> data) {
            super.onLoadFinished(loader, data);
            placeUsersAdapter.clearSection(UserPlaceStatusEnum.COMING);
            placeUsersAdapter.clearSection(UserPlaceStatusEnum.HERE);
            placeUsersAdapter.addData(data);
            mAdapter.notifyDataSetChanged();
            noPostsView.setVisibility(placeUsersAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }

    class InviteSentLoader extends MultipleEntryLoaderCallback<EventsInvitation> {

        public InviteSentLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_EVENT_INVITED,
                    MyApplication.getCurrentUser().getInviteSentQuery(event.getId()));

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout, false);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            placeUsersAdapter.clearSection(UserPlaceStatusEnum.INVITED);
            placeUsersAdapter.addData(UserPlaceStatusEnum.INVITED, data);
            mAdapter.notifyDataSetChanged();
            noPostsView.setVisibility(placeUsersAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }
}
