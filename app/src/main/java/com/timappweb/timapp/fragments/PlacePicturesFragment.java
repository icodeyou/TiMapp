package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import retrofit2.Call;
import retrofit2.Response;


public class PlacePicturesFragment extends Fragment {

    private static final String TAG = "PlacePicturesFragment";

    private PlaceActivity placeActivity;
    private Context context;

    private Place place;
    private int placeId;

    //Views
    private View                    progressView;
    private View                    noPicView;
    private View                    noConnectionView;
    private View mainButton;
    private View                    smallTagsButton;
    private TextView                tvAddButton;
    private RecyclerView            picturesRv;
    private View                    uploadView;

    private PicturesAdapter         picturesAdapter;

    private static int NUMBER_OF_COLUMNS =  2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_pictures, container, false);
        context = placeActivity;

        initVariables(root);

        setListeners();
        initRv();
        initAdapter();

        placeActivity.notifyFragmentsLoaded();

        this.loadPictures();

        return root;
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    private void initVariables(View root) {
        placeActivity = (PlaceActivity) getActivity();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        //Views
        mainButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        smallTagsButton = root.findViewById(R.id.button_add_tags);
        progressView = root.findViewById(R.id.progress_view);
        noPicView = root.findViewById(R.id.no_pictures_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        uploadView = root.findViewById(R.id.upload_view);
        picturesRv = (RecyclerView) root.findViewById(R.id.pictures_rv);
    }

    private void setListeners() {
        mainButton.setOnClickListener(placeActivity.getPictureListener());
        smallTagsButton.setOnClickListener(placeActivity.getTagListener());
    }

    private void initRv() {
        //picturesRv.setHasFixedSize(true);
        GridLayoutManager layoutManager =
                new GridLayoutManager(context, NUMBER_OF_COLUMNS);
        picturesRv.setLayoutManager(layoutManager);
    }

    private void initAdapter() {
        picturesAdapter = new PicturesAdapter(placeActivity);
        picturesRv.setAdapter(picturesAdapter);
        picturesAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                //TODO : Display picture in another activity, to see it fullscreen
                //TODO later : It'd be great if then we can scroll pics.. Loading them each time we scroll.
            }
        });
    }


    //Public methods
    //////////////////////////////////////////////////////////////////////////////

    public void loadPictures(){
        Log.d(TAG, "Loading places pictures");
        Call<PaginationResponse<Picture>> call = RestClient.service().viewPicturesForPlace(this.placeId);

        call.enqueue(new RestCallback<PaginationResponse<Picture>>(this.getContext()) {

            @Override
            public void onResponse(Response<PaginationResponse<Picture>> response) {
                super.onResponse(response);

                if (response.isSuccess()) {
                    PaginationResponse<Picture> paginationData = response.body();
                    Log.d(TAG, "Loading " + paginationData.total + " picture(s) with base url: " + paginationData.extra.get("base_url"));
                    //picturesAdapter.addDummyData();
                    picturesAdapter.setBaseUrl(paginationData.extra.get("base_url"));
                    picturesAdapter.setData(paginationData.items);
                    picturesAdapter.notifyDataSetChanged();
                }

                setProgressView(false);

                if (picturesAdapter.getItemCount() == 0) {
                    noPicView.setVisibility(View.VISIBLE);
                    picturesRv.setVisibility(View.GONE);
                } else {
                    noPicView.setVisibility(View.GONE);
                    picturesRv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setSmallTagsButtonVisibility(boolean bool) {
        if(bool) {
            smallTagsButton.setVisibility(View.VISIBLE);
        }
        else {
            smallTagsButton.setVisibility(View.GONE);
        }
    }

    public View getSmallTagsButton() {
        return smallTagsButton;
    }

    public void setMainButtonVisibility(boolean bool) {
        if(bool) {
            mainButton.setVisibility(View.VISIBLE);
        }
        else {
            mainButton.setVisibility(View.GONE);
        }
    }

    public View getMainButton() {
        return mainButton;
    }

    public TextView getTvMainButton() {
        return tvAddButton;
    }

    public PicturesAdapter getPicAdapter(){
        return picturesAdapter;
    }

    public RecyclerView getPicturesRv(){
        return picturesRv;
    }

    public void setUploadVisibility(Boolean bool) {
        if(bool) {
            noPicView.setVisibility(View.GONE);
            uploadView.setVisibility(View.VISIBLE);
        } else {
            uploadView.setVisibility(View.GONE);
        }
    }

    public void setProgressView(boolean visibility) {
        if(visibility) {
            progressView.setVisibility(View.VISIBLE);
            picturesRv.setVisibility(View.GONE);
            noPicView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            picturesRv.setVisibility(View.VISIBLE);
            noPicView.setVisibility(View.GONE);
        }
    }

    public void updateBtnVisibility() {
        Log.v(TAG, "::updateButtonsVisibility()");
        // Check if the user can post in this place
        boolean showMainButton = place != null && MyApplication.hasLastLocation()
                && CacheData.isAllowedToAddPicture() && place.isAround();
        mainButton.setVisibility(showMainButton ? View.VISIBLE : View.GONE);
        smallTagsButton.setVisibility(!showMainButton && place != null && place.isAround() ? View.VISIBLE : View.GONE);
    }
}
