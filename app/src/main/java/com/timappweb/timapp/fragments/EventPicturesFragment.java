package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.PictureInterface;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.sync.performers.SyncAdapterOption;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.loaders.ModelLoader;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class EventPicturesFragment extends EventBaseFragment implements LocationManager.LocationListener, OnTabSelectedListener {

    private static final String         TAG                             = "EventPicturesFragment";
    public static int                   PICUTRE_GRID_COLUMN_NB          =  2;
    private static final long MIN_DELAY_REFRESH = 3600 * 1000;
    private static final int            ENDLESS_SCROLL_THRESHOLD = 1;

    // ---------------------------------------------------------------------------------------------

    private Context                     context;

    //Views
    //private View                      progressView;
    private View                        noPicView;
    private View                        noConnectionView;
    private View                        uploadView;

    private PicturesAdapter             picturesAdapter;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private FloatingActionButton        mPostButton;
    private RefreshableRecyclerView     mRecyclerView;
    //private RecyclerViewMaterialAdapter           mAdapterWrapper;
    private Loader<List<Picture>>       mLoader;
    private PictureLoader mPictureLoaderModel;

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        context = getContext();
        //Views
        noPicView = root.findViewById(R.id.no_pictures_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, PICUTRE_GRID_COLUMN_NB));
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);
        mPictureLoaderModel = new PictureLoader();
        mLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_PICTURE, null, mPictureLoaderModel);
        mSwipeRefreshLayout.setOnRefreshListener(mPictureLoaderModel);
        startPictureActivity();
        initAdapter();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            if (eventActivity.getEvent() == null){
                throw new Exception("Cannot add picture for null event");
            }
            eventActivity.getEvent().requireLocalId();
        } catch (Exception e) {
            IntentsUtils.home(getContext());
        }
        super.onViewCreated(view, savedInstanceState);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncResult(SyncResultMessage event) {
        picturesAdapter.onLoadMoreComplete(null);
        mSwipeRefreshLayout.setRefreshing(false);
        if (event.upToDate){
            Log.d(TAG, "All data are up to date, disabling load more");
            picturesAdapter.removeLoadMore();
        }
    }

    private void initAdapter() {

        picturesAdapter = new PicturesAdapter(getActivity(), PICUTRE_GRID_COLUMN_NB);
        //Experimenting NEW features (v5.0.0)
        picturesAdapter.setAutoScrollOnExpand(true);
        picturesAdapter.setHandleDragEnabled(true);
        picturesAdapter.setAnimationOnScrolling(true);
        picturesAdapter.setAnimationOnReverseScrolling(true);
        mRecyclerView.setAdapter(picturesAdapter);
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                //NOTE: This allows to receive Payload objects when notifyItemChanged is called by the Adapter!!!
                return true;
            }
        });
        //mListener.onFragmentChange(mSwipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);
        picturesAdapter.setEndlessScrollListener(mPictureLoaderModel, new ProgressItem());
        picturesAdapter.setEndlessScrollThreshold(ENDLESS_SCROLL_THRESHOLD);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }


    public void setUploadVisibility(Boolean bool) {
        if(bool) {
            noPicView.setVisibility(View.GONE);
            uploadView.setVisibility(View.VISIBLE);
        } else {
            uploadView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void uploadPicture(final Uri fileUri) {
        setUploadVisibility(true);
        // create upload service client
        File file = new File(fileUri.getPath());

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
                .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser(), QuotaType.ADD_PICTURE))
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        // Get the bitmap in according to the width of the device
                        mRecyclerView.smoothScrollToPosition(0);
                        picture.mySaveSafeCall();
                        Log.d(TAG, "New picture uploaded: " + picture);
                    }

                })
                .onError(new RequestFailureCallback(){
                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(EventPicturesFragment.this.getContext(),
                                R.string.cannot_upload_picture, Toast.LENGTH_LONG).show();
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        setUploadVisibility(false);
                    }

                })
                .perform();

        } catch (IOException e) {
            Toast.makeText(this.getContext(), R.string.cannot_resize_picture, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            return ;
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        LocationManager.addOnLocationChangedListener(this);


    }
    @Override
    public void onPause() {
        super.onResume();
        LocationManager.removeLocationListener(this);
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        //mPostButton.setVisibility(eventActivity.isUserAround() ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onTabSelected() {
        mRecyclerView.smoothScrollToPosition(0);
    }


    // =============================================================================================

    /**
     */
    class PictureLoader implements FlexibleAdapter.EndlessScrollListener, LoaderManager.LoaderCallbacks<List<Picture>>, SwipeRefreshLayout.OnRefreshListener {

        @Override
        public Loader<List<Picture>> onCreateLoader(int id, Bundle args) {
            ModelLoader loader = new ModelLoader(context, Picture.class, eventActivity.getEvent().getPicturesQuery(), false);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<Picture>> loader, List<Picture> data) {
            //picturesAdapter.setBaseUrl(this.getServerResponse().extra.get("base_url"));
            picturesAdapter.setData(data);
            picturesAdapter.notifyDataSetChanged();
            noPicView.setVisibility(!picturesAdapter.hadData() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<List<Picture>> loader) {

        }

        @Override
        public void onLoadMore() {
            Log.d(TAG, "Loading more data");
            long remoteId = SyncBaseModel.getMinRemoteId(Picture.class, "Event = " + eventActivity.getEvent().getId());
            Log.d(TAG, "Last picture has been created: " + remoteId);
            SyncAdapterOption params = new SyncAdapterOption();
            if (remoteId > 0){
                params.setMaxId(remoteId-1);
            }
            params.setDirection(SyncAdapterOption.SyncDirection.DOWN)
                    .setType(DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE)
                    .getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, eventActivity.getEvent().getRemoteId());

            SyncBaseModel.startSync(EventPicturesFragment.this.getContext(), params);
        }

        @Override
        public void onRefresh() {
            if (!SyncHistory.requireUpdate(DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE, getEvent(), MIN_DELAY_REFRESH)){
                return;
            }

            long remoteId = SyncBaseModel.getMaxRemoteId(Picture.class, "Event = " + eventActivity.getEvent().getId());
            SyncAdapterOption params = new SyncAdapterOption();
            params.setMinId(remoteId+1)
                    .setDirection(SyncAdapterOption.SyncDirection.DOWN)
                    .setType(DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE)
                    .getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, eventActivity.getEvent().getRemoteId());
            SyncBaseModel.startSync(EventPicturesFragment.this.getContext(), params);
        }
    }
}

