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

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.EventUsersHeaderAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.parallaxviewpager.RecyclerViewFragment;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class EventPeopleFragment extends EventBaseFragment {

    private static final String TAG = "EventTagsFragment";

    private Context         context;
    private EventActivity eventActivity;

    private EventUsersHeaderAdapter placeUsersAdapter;

    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton postButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_people, container, false);
        eventActivity = (EventActivity) getActivity();
        context= eventActivity.getBaseContext();

        //Views

        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.list_people);
        postButton = (FloatingActionButton) root.findViewById(R.id.post_button);

        initAdapter();

        setupRecyclerView();

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


        return root;
    }


    private void initAdapter() {
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
    }


    private void loadPosts() {
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(eventActivity.getEventId());
        RestCallback callback = new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse200(Response<List<Post>> response) {
                List<Post> list = response.body();
                placeUsersAdapter.addData(UserPlaceStatusEnum.HERE, list);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                noConnectionView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        };
        //asynCalls.add(ApiCallFactory.build(call, callback, this));
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

    // =============================================================================================
    // PARALLAX VIEW
    @Override
    protected void setScrollOnLayoutManager(int scrollY) {
        ((LinearLayoutManager)mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, -scrollY);
    }

    @Override
    protected void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(placeUsersAdapter)); // Add the sticky headers decoration
        mRecyclerView.setAdapter(placeUsersAdapter);
        setRecyclerViewOnScrollListener();
    }


    // =============================================================================================

    /**
     * TODO
     */
    class UserStatusLoader extends MultipleEntryLoaderCallback<UserEvent> {

        public UserStatusLoader(Context context, Event event) {
            super(context, 3600 * 1000, DataSyncAdapter.SYNC_TYPE_EVENT_USERS, UserEvent.queryForPlace(event));
            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout, false);
        }

        @Override
        public void onLoadFinished(Loader<List<UserEvent>> loader, List<UserEvent> data) {
            super.onLoadFinished(loader, data);
            placeUsersAdapter.clearSection(UserPlaceStatusEnum.COMING);
            placeUsersAdapter.clearSection(UserPlaceStatusEnum.HERE);
            placeUsersAdapter.addData(data);
            placeUsersAdapter.addData(data); // TODO remove
            placeUsersAdapter.notifyDataSetChanged();
            noPostsView.setVisibility(placeUsersAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }
    /**
     * TODO
     */
    class InviteSentLoader extends MultipleEntryLoaderCallback<EventsInvitation> {

        public InviteSentLoader(Context context, Event event) {
            super(context, 3600 * 1000,
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
            placeUsersAdapter.notifyDataSetChanged();
            noPostsView.setVisibility(placeUsersAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }
}
