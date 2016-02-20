package com.timappweb.timapp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;

import retrofit2.Call;


public class PlacePicturesFragment extends Fragment {

    private static final String TAG = "PlacePicturesFragment";

    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    //Views
    private View                    progressView;
    private View                    noTagsView;
    private View                    noConnectionView;
    private View                    addButton;
    private View                    smallTagsButton;
    private ImageView               pictureTaken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_pictures, container, false);

        initVariables(root);

        setListeners();

        placeActivity.notifyFragmentsLoaded();

        return root;
    }

    private void initVariables(View root) {
        placeActivity = (PlaceActivity) getActivity();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        //Views
        addButton = root.findViewById(R.id.main_button_pics);
        smallTagsButton = root.findViewById(R.id.button_add_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        pictureTaken = (ImageView) root.findViewById(R.id.picture_taken);
    }

    private void setListeners() {

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //placeActivity.requestForCameraPermission();
                placeActivity.takePicture();
            }
        });
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

    public void setMainButtonVisibility(boolean bool) {
        if(bool) {
            addButton.setVisibility(View.VISIBLE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
    }

    public void setImage(Bitmap bitmap) {
        pictureTaken.setImageBitmap(bitmap);
    }

}
