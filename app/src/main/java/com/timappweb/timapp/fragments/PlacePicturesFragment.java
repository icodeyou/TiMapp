package com.timappweb.timapp.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

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
    private View                    noTagsView;
    private View                    noConnectionView;
    private View                    addButton;
    private View                    smallTagsButton;
    private ImageView               pictureTaken;
    private TextView                tvAddButton;
    private RecyclerView            picturesRv;

    private PicturesAdapter         picturesAdapter;

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

    private void initRv() {
        GridLayoutManager layoutManager =
                new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, true);
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


    public void loadPictures(){
        Log.d(TAG, "Loading places pictures");
        Call<List<Picture>> call = RestClient.service().viewPicturesForPlace(1);

        call.enqueue(new RestCallback<List<Picture>>(this.getContext()) {

            @Override
            public void onResponse(Response<List<Picture>> response) {
                super.onResponse(response);

                if (response.isSuccess()) {
                    List<Picture> pictures = response.body();
                    picturesAdapter.addDummyData();
                    //picturesAdapter.setData(pictures);
                }

                progressView.setVisibility(View.GONE);
            }

        });

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
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        //Views
        addButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        smallTagsButton = root.findViewById(R.id.button_add_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        pictureTaken = (ImageView) root.findViewById(R.id.picture_taken);
        picturesRv = (RecyclerView) root.findViewById(R.id.pictures_rv);
    }

    private void setListeners() {
        addButton.setOnClickListener(placeActivity.getPictureListener());
        smallTagsButton.setOnClickListener(placeActivity.getTagListener());
    }

    //Public methods
    //////////////////////////////////////////////////////////////////////////////

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
            addButton.setVisibility(View.VISIBLE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
    }

    public View getMainButton() {
        return addButton;
    }

    public TextView getTvMainButton() {
        return tvAddButton;
    }

    public void setImage(Bitmap bitmap) {
        pictureTaken.setImageBitmap(bitmap);
    }

}
