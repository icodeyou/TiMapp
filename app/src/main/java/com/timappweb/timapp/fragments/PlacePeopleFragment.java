package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PlaceUsersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.PlacesInvitation;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.adapters.SimpleSectionedRecyclerViewAdapter;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


public class PlacePeopleFragment extends BaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    private Context         context;
    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    private PlaceUsersAdapter placeUsersAdapter;
    private RecyclerView    peopleRv;
    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private View            mainButton;
    private TextView        tvAddButton;

    //private List<Post> posts;
    //private List<UserPlace> peopleComing;
    //private List<UserPlace> peopleInvited;

   // private ArrayList<PlaceUserInterface> usersFullList;
    private DeferredObject deferred;
    private int loadCount = 0;
    private SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_people, container, false);

        initVariables(root);
        setListeners();
        initRv();
        initAdapter();

        load();

        placeActivity.notifyFragmentsLoaded();

        return root;
    }



    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    private void initVariables(View root) {
        placeActivity = (PlaceActivity) getActivity();
        context= placeActivity.getBaseContext();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();


        //Views
        mainButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        peopleRv = (RecyclerView) root.findViewById(R.id.list_people);
        progressView = root.findViewById(R.id.progress_view);
        noPostsView = root.findViewById(R.id.no_posts_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
    }

    private void setListeners() {
        mainButton.setOnClickListener(placeActivity.getPeopleListener());
    }

    private void initRv() {
        peopleRv.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initAdapter() {
        //Construct Adapter
        placeUsersAdapter = new PlaceUsersAdapter(context);
        placeUsersAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.v(TAG, "Accessing position: " + position);
                PlaceUserInterface user = placeUsersAdapter.get(position);
                Log.d(TAG, "Viewing profile user: " + user.getUser());
                IntentsUtils.profile(placeActivity, user.getUser());
            }
        });
        placeUsersAdapter.create("post", getResources().getString(R.string.header_posts));
        placeUsersAdapter.create(UserPlaceStatus.COMING, getResources().getString(R.string.header_coming));
        placeUsersAdapter.create(UserPlaceStatus.INVITED, getResources().getString(R.string.header_invited));

        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(
                context,
                R.layout.header_place_people,
                R.id.text_header_place_people,
                placeUsersAdapter);
        peopleRv.setAdapter(mSectionedAdapter);
    }

    public void load() {
        placeUsersAdapter.clear();
        loadPosts();
        if (MyApplication.isLoggedIn()){
            loadInvites();
        }
        loadByStatus(UserPlaceStatus.COMING);
    }


    private void loadPosts() {
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse(Response<List<Post>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    setProgressView(false);
                    notifyPostsLoaded(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                setProgressView(false);
                noConnectionView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        });
        asynCalls.add(call);
    }


    private void loadByStatus(final UserPlaceStatus status){
        Map<String, String> conditions = new HashMap<>();
        conditions.put("status", String.valueOf(status));

        Call<PaginationResponse<UserPlace>> call = RestClient.service().viewUsersForPlace(placeActivity.getPlaceId(), conditions);
        call.enqueue(new RestCallback<PaginationResponse<UserPlace>>(getContext()) {
            @Override
            public void onResponse(Response<PaginationResponse<UserPlace>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    notifyUsersStatusLoaded(status, response.body().items);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                setProgressView(false);
                noConnectionView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        });
    }

    private void loadInvites(){
        Call<PaginationResponse<PlacesInvitation>> call = RestClient.service().invitesSent(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<PaginationResponse<PlacesInvitation>>(getContext()) {

            @Override
            public void onResponse200(Response<PaginationResponse<PlacesInvitation>> response) {
                List<PlacesInvitation> invitations = response.body().items;
                Log.d(TAG, "Loading " + invitations.size() + " invites sent");
                notifyUserInvitedLoaded(invitations);
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }

        });
    }



    private void notifyPostsLoaded(List<Post> items) {
        placeUsersAdapter.add("post", items);
        notifyDataChanged();
    }
    private void notifyUsersStatusLoaded(UserPlaceStatus status, List<UserPlace> items) {
        placeUsersAdapter.add(status, items);
        notifyDataChanged();
    }
    private void notifyUserInvitedLoaded(List<PlacesInvitation> items) {
        Log.d(TAG, "Adding " + items.size() + " invitation(s)");
        placeUsersAdapter.add(UserPlaceStatus.INVITED, items);
        notifyDataChanged();
    }

    private void notifyDataChanged(){
        Log.d(TAG, "Adapter size: " + placeUsersAdapter.getItemCount());
        placeUsersAdapter.notifyDataSetChanged();
        mSectionedAdapter.setSections(placeUsersAdapter.buildSections());
    }

    public void updateBtnVisibility() {
        boolean showMainButton = place != null && MyApplication.hasLastLocation() && place.isAround();
        mainButton.setVisibility(showMainButton ? View.VISIBLE : View.GONE);
    }

    public TextView getTvMainButton() {
        return tvAddButton;
    }

    public View getMainButton() {
        return mainButton;
    }

    public void setProgressView(boolean visibility) {
        if(visibility) {
            progressView.setVisibility(View.VISIBLE);
            peopleRv.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            peopleRv.setVisibility(View.VISIBLE);
            noConnectionView.setVisibility(View.GONE);
        }
    }
}
