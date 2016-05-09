package com.timappweb.timapp.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.ImageUtility;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class PlacePicturesFragment extends PlaceBaseFragment {

    private static final String TAG = "PlacePicturesFragment";

    private EventActivity eventActivity;
    private Context context;

    //Views
    private View                    progressView;
    private View                    noPicView;
    private View                    noConnectionView;
    private View                    mainButton;
    private View                    smallTagsButton;
    private TextView                tvAddButton;
    private RecyclerView            picturesRv;
    private View                    uploadView;

    private PicturesAdapter         picturesAdapter;

    private static int NUMBER_OF_COLUMNS =  1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        context = eventActivity;

        initVariables(root);

        setListeners();
        initRv();
        initAdapter();

        this.loadData();

        updateBtnVisibility();

        return root;
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    private void initVariables(View root) {
        eventActivity = (EventActivity) getActivity();

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
        mainButton.setOnClickListener(eventActivity.getPictureListener());
        smallTagsButton.setOnClickListener(eventActivity.getTagListener());
    }

    private void initRv() {
        //picturesRv.setHasFixedSize(true);
        GridLayoutManager layoutManager =
                new GridLayoutManager(context, NUMBER_OF_COLUMNS);
        picturesRv.setLayoutManager(layoutManager);
    }

    private void initAdapter() {
        picturesAdapter = new PicturesAdapter(eventActivity);
        picturesRv.setAdapter(picturesAdapter);
    }


    //Public methods
    //////////////////////////////////////////////////////////////////////////////

    public void loadData(){
        Log.d(TAG, "Loading places pictures");
        Call<PaginationResponse<Picture>> call = RestClient.service().viewPicturesForPlace(eventActivity.getEventId());
        RestCallback callback = new RestCallback<PaginationResponse<Picture>>(this.getContext(), this) {
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

                if (picturesAdapter.getItemCount() == 0) {
                    noPicView.setVisibility(View.VISIBLE);
                    picturesRv.setVisibility(View.GONE);
                } else {
                    noPicView.setVisibility(View.GONE);
                    picturesRv.setVisibility(View.VISIBLE);
                }
            }

        };
        asynCalls.add(ApiCallFactory.build(call, callback, this));
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

    public void uploadPicture(final Uri fileUri) {
        // create upload service client
        File file = new File(fileUri.getPath());

        LoadingListener pictureLoadListener = new LoadingListener() {
            @Override
            public void onLoadStart() {
                setUploadVisibility(true);
                updateBtnVisibility();
            }

            @Override
            public void onLoadEnd() {
                setUploadVisibility(false);
                updateBtnVisibility();
            }
        };

        try {
            // Compress the file
            Log.d(TAG, "BEFORE COMPRESSION: " +
                    "Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    ". Max size: " + Util.byteToKB(ConfigurationProvider.rules().picture_max_size));

            ApplicationRules rules = ConfigurationProvider.rules();
            file = PictureUtility.resize(context, file, rules.picture_max_width, rules.picture_max_height);

            MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));

            Log.d(TAG, "AFTER COMPRESSION: Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    " and type: " + fileMimeType);

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("photo", file.getName(),
                            RequestBody.create(fileMimeType, file))
                    .build();

            // finally, execute the request
            Call<RestFeedback> call = RestClient.service().upload(eventActivity.getEventId(), body);
            RestCallback callback = new RestCallback<RestFeedback>(eventActivity, pictureLoadListener) {
                @Override
                public void onResponse200(Response<RestFeedback> response) {
                    RestFeedback feedback = response.body();

                    if (feedback.success) {
                        Log.v(TAG, "SUCCESS UPLOAD IMAGE");
                        // Get the bitmap in according to the width of the device
                        Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(fileUri.getPath(), 1000, 1000);
                        getPicturesRv().smoothScrollToPosition(0);
                        QuotaManager.instance().add(QuotaType.ADD_PICTURE);
                        loadData();
                    } else {
                        Log.v(TAG, "FAILURE UPLOAD IMAGE: " + feedback.message);
                    }
                    Toast.makeText(eventActivity, feedback.message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Upload error:" + t.getMessage());
                    Toast.makeText(context, "We cannot upload this image", Toast.LENGTH_LONG).show();
                }

            };


            asynCalls.add(ApiCallFactory.build(call, callback, pictureLoadListener));

        } catch (IOException e) {
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            return ;
        }
    }


    public void updateBtnVisibility() {
        Log.v(TAG, "::updateBtnVisibility()");
        // Check if the user can post in this place
        boolean isUserAround = eventActivity.isUserAround();
        boolean isAllowedToAddPost = QuotaManager.instance().checkQuota(QuotaType.ADD_POST);
        boolean isAllowedToAddPic = QuotaManager.instance().checkQuota(QuotaType.ADD_PICTURE) && uploadView.getVisibility() != View.VISIBLE;
        boolean showMainButton = isUserAround && isAllowedToAddPic;
        mainButton.setVisibility(showMainButton ? View.VISIBLE : View.GONE);
        smallTagsButton.setVisibility(!showMainButton && isUserAround && isAllowedToAddPost ? View.VISIBLE : View.GONE);
    }


}

