package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.services.PictureInterface;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;


public class EventPicturesFragment extends EventBaseFragment {

    private static final String TAG = "EventPicturesFragment";

    private EventActivity eventActivity;
    private Context context;

    //Views
    //private View                    progressView;
    private View                    noPicView;
    private View                    noConnectionView;
    private View                    uploadView;

    private PicturesAdapter         picturesAdapter;

    public static int PICUTRE_GRID_COLUMN_NB =  2;
    private static final long MAX_UPDATE_DELAY = 3600 * 1000;
    
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton postButton;
    private RefreshableRecyclerView mRecyclerView;
    private RecyclerViewMaterialAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        context = getContext();
        eventActivity = (EventActivity) getActivity();
        //Views
        // progressView = root.findViewById(R.id.progress_view);
        noPicView = root.findViewById(R.id.no_pictures_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, PICUTRE_GRID_COLUMN_NB));
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);
        postButton = (FloatingActionButton) root.findViewById(R.id.post_button);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), eventActivity.getEvent(), IntentsUtils.ACTION_CAMERA);
            }
        });
        initAdapter();
        //this.loadData();

        getLoaderManager().initLoader(EventActivity.LOADER_ID_PICTURE, null, new PictureLoader(this.getContext(), eventActivity.getEvent()));

        startPictureActivity();

        return root;
    }

    private void startPictureActivity() {
        Bundle extras = eventActivity.getIntent().getExtras();
        if(extras!=null && extras.getInt(IntentsUtils.KEY_ACTION, -1) == IntentsUtils.ACTION_CAMERA) {
            IntentsUtils.addPictureFromFragment(getActivity(), this);
            eventActivity.getIntent().putExtra(IntentsUtils.KEY_ACTION, -1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==IntentsUtils.REQUEST_CAMERA) {
            Log.d(TAG, "Result request camera");
            eventActivity.setCurrentPageSelected(EventActivity.PAGER_PICTURE);
            if (resultCode != Activity.RESULT_OK){
                Log.e(TAG, "Activity result for requesting camera returned a non success code: " + resultCode);
                return;
            }
            Uri photoUri = data.getData();
            uploadPicture(photoUri);
        }
    }

    private void initAdapter() {
        picturesAdapter = new PicturesAdapter(eventActivity);
        mAdapter = new RecyclerViewMaterialAdapter(picturesAdapter, PICUTRE_GRID_COLUMN_NB);
        mRecyclerView.setAdapter(mAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }


    //Public methods
    //////////////////////////////////////////////////////////////////////////////
    /*
    public void loadData(){
        Log.d(TAG, "Loading places pictures");
        Call<PaginatedResponse<Picture>> call = RestClient.service().viewPicturesForPlace(eventActivity.getEventId());
        RestCallback callback = new RestCallback<PaginatedResponse<Picture>>(this.getContext(), this) {
            @Override
            public void onResponse(Response<PaginatedResponse<Picture>> response) {
                super.onResponse(response);

                if (response.isSuccess()) {
                    PaginatedResponse<Picture> paginationData = response.body();
                    Log.d(TAG, "Loading " + paginationData.total + " picture(s) with base url: " + paginationData.extra.get("base_url"));
                    //picturesAdapter.addDummyData();
                    picturesAdapter.setBaseUrl(paginationData.extra.get("base_url"));
                    picturesAdapter.setData(paginationData.items);
                    picturesAdapter.notifyDataSetChanged();

                    if (picturesAdapter.getItemCount() == 0) {
                        noPicView.setVisibility(View.VISIBLE);
                        picturesRv.setVisibility(View.GONE);
                    } else {
                        noPicView.setVisibility(View.GONE);
                        picturesRv.setVisibility(View.VISIBLE);
                    }
                    startPictureActivity();
                }
            }

        };
        asynCalls.add(ApiCallFactory.build(call, callback, this));
    }
    */
    public RecyclerView getPicturesRv(){
        return mRecyclerView;
    }


    public void setUploadVisibility(Boolean bool) {
        if(bool) {
            noPicView.setVisibility(View.GONE);
            uploadView.setVisibility(View.VISIBLE);
        } else {
            uploadView.setVisibility(View.GONE);
        }
    }

    /*
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
    }*/

    public void uploadPicture(final Uri fileUri) {
        // create upload service client
        File file = new File(fileUri.getPath());

        LoadingListener pictureLoadListener = new LoadingListener() {
            @Override
            public void onLoadStart() {
                setUploadVisibility(true);
            }

            @Override
            public void onLoadEnd() {
                setUploadVisibility(false);
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

            final Event event = eventActivity.getEvent();
            final Picture picture = new Picture();
            picture.setEvent(event);
            picture.setUser(MyApplication.getCurrentUser());

            Call call = RestClient.instance().createService(PictureInterface.class).upload(event.getRemoteId(), body);
            RestClient
                .buildCall(call)
                .onResponse(new AutoMergeCallback(picture))
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        // Get the bitmap in according to the width of the device
                        getPicturesRv().smoothScrollToPosition(0);
                        QuotaManager.instance().add(QuotaType.ADD_PICTURE);
                        picture.mySave();
                        Log.v(TAG, "New picture uploaded: " + picture);
                    }

                })
                .perform();

        } catch (IOException e) {
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            return ;
        }
    }



    // =============================================================================================

    /**
     * TODO
     */
    class PictureLoader extends MultipleEntryLoaderCallback<Picture> {

        public PictureLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY, DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE, event.getPicturesQuery());
            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout);
        }

        @Override
        public void onLoadFinished(Loader<List<Picture>> loader, List<Picture> data) {
            super.onLoadFinished(loader, data);
            //picturesAdapter.setBaseUrl(this.getServerResponse().extra.get("base_url"));
            picturesAdapter.setData(data);
            mAdapter.notifyDataSetChanged();
            noPicView.setVisibility(picturesAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }
}

