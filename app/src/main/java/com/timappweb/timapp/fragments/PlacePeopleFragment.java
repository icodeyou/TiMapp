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
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.adapters.SimpleSectionedRecyclerViewAdapter;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
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
    private RecyclerView peopleRv;
    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private View            addButton;
    private TextView        tvAddButton;

    private List<Post> posts;
    private List<UserPlace> peopleComing;
    private List<UserPlace> peopleInvited;

    private ArrayList<PlaceUserInterface> usersFullList;

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
        if (visible) {
            if(addButton!=null) {
                placeActivity.setPlusButtonVisibility(addButton.getVisibility()==View.VISIBLE);
            }
        }
    }

    private void initVariables(View root) {
        placeActivity = (PlaceActivity) getActivity();
        context= placeActivity.getBaseContext();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        usersFullList = new ArrayList<>();

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
        peopleRv.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initAdapter() {
        //Construct Adapter
        placeUsersAdapter = new PlaceUsersAdapter(context);

        placeUsersAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                User user = placeUsersAdapter.getInterface(position).getUser();
                Log.d(TAG, "Viewing profile user: " + user);
                IntentsUtils.profile(placeActivity, user);
            }
        });
    }

    private void load() {
        usersFullList.clear();
        loadPosts();
    }


    private void loadPosts() {
        Call<List<Post>> call = RestClient.service().viewPostsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Post>>(getContext()) {
            @Override
            public void onResponse(Response<List<Post>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    posts = response.body();
                    progressView.setVisibility(View.GONE);
                    notifyPostsLoaded(posts);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                progressView.setVisibility(View.GONE);
                noConnectionView.setVisibility(View.VISIBLE);
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
                    PaginationResponse<UserPlace> paginateData = response.body();
                    if (status == UserPlaceStatus.COMING) {
                        peopleComing = paginateData.items;
                        notifyUsersComingLoaded(peopleComing);
                    } else if (status == UserPlaceStatus.INVITED) {
                        peopleInvited = paginateData.items;
                        notifyUsersInvitedLoaded(peopleInvited);
                    }
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

    private void notifyPostsLoaded(List<Post> items) {
        for (Post post : items) {
            PlaceUserInterface placeUserInterface = post;
            usersFullList.add(placeUserInterface);
        }
        loadByStatus(UserPlaceStatus.COMING);
    }
    private synchronized void notifyUsersComingLoaded(List<UserPlace> comingUsers) {
        for (UserPlace comingUser : comingUsers) {
            Log.d(TAG, "Coming user: " + comingUser);
            usersFullList.add(comingUser);
        }
        loadByStatus(UserPlaceStatus.INVITED);
        setAdapter();
    }

    private void notifyUsersInvitedLoaded(List<UserPlace> invitedUsers) {
        for (UserPlace invitedUser : invitedUsers) {
            usersFullList.add(invitedUser);
        }
    }

    private void setAdapter() {
        //TODO : if empty ==> no posts view
        //SET LIST IN ADAPTER
        placeUsersAdapter.setData(usersFullList);

        //This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<SimpleSectionedRecyclerViewAdapter.Section>();

        //Sections
        if(posts!=null) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(
                    0,getString(R.string.header_posts)));
        }
        if(peopleComing!=null) {
            if(peopleComing.size()!=0) {
                int numberPosts = posts==null ? 0 : posts.size();
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(
                        numberPosts,getString(R.string.header_coming)));
            }
        }
        if(peopleInvited!=null) {
            if (peopleInvited.size()!=0) {
                int numberPosts = posts==null ? 0 : posts.size();
                int numberComing = peopleComing==null ? 0 : peopleComing.size();
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(
                        numberPosts+numberComing,getString(R.string.header_invited)));
            }
        }

        //Add your adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                SimpleSectionedRecyclerViewAdapter(context,R.layout.header_place_people,
                R.id.text_header_place_people,placeUsersAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        peopleRv.setAdapter(mSectionedAdapter);
        Log.i(TAG, context.getString(R.string.log_set_adapter_placepeoplefragment)
                + usersFullList.size());
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
