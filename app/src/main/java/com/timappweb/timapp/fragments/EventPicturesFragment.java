package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
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
import com.timappweb.timapp.data.loader.DataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.PictureInterface;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class EventPicturesFragment extends EventBaseFragment implements LocationManager.LocationListener, OnTabSelectedListener {

    private static final String         TAG                             = "EventPicturesFragment";
    public static int                   PICUTRE_GRID_COLUMN_NB          = 2;
    private static final long           MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final long           MIN_DELAY_AUTO_REFRESH          = 10 * 60 * 1000;
    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    // ---------------------------------------------------------------------------------------------

    private View                        noPicView;
    private View                        noConnectionView;
    private View                        uploadView;
    private PicturesAdapter             picturesAdapter;
    private WaveSwipeRefreshLayout          mSwipeRefreshLayout;
    private FloatingActionButton        mPostButton;
    private RefreshableRecyclerView     mRecyclerView;
    private Loader<List<Picture>>       mLoader;
    private DataLoader                  mPictureLoaderModel;

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        //Views
        noPicView = root.findViewById(R.id.no_pictures_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), PICUTRE_GRID_COLUMN_NB));
        mSwipeRefreshLayout = (WaveSwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);
        mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(getContext(),R.color.colorRefresh));
        mPictureLoaderModel = new PictureLoader(getContext());
        mLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_PICTURE, null, mPictureLoaderModel);
        startPictureActivity();
        initAdapter();

        SyncAdapterOption options = new SyncAdapterOption()
                .setType(DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE);
        options.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, eventActivity.getEvent().getRemoteId());

        mPictureLoaderModel = new PictureLoader(this.getContext())
                .setEnlessLoading(picturesAdapter)
                .setSwipeAndRefreshLayout(mSwipeRefreshLayout)
                .setHistoryItemInterface(getEvent())
                .setSyncOptions(options)
                .setMinDelayAutoRefresh(MIN_DELAY_AUTO_REFRESH)
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH);
        mLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_PICTURE, null, mPictureLoaderModel);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            if (eventActivity.getEvent() == null){
                throw new Exception("Cannot add picture for null event");
            }
            eventActivity.setEvent((Event) eventActivity.getEvent().requireLocalId());
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
    /**
     * Only one upload at a time.
     * @param fileUri
     */
    public void uploadPicture(final Uri fileUri) {
        if (uploadView.getVisibility() == View.VISIBLE){
            Toast.makeText(getContext(), R.string.upload_picture_in_progress, Toast.LENGTH_SHORT).show();
            return;
        }
        setUploadVisibility(true);
        // create upload service client
        File file = new File(fileUri.getPath());

        try {
            ApplicationRules rules = ConfigurationProvider.rules();
            // Compress the file
            Log.d(TAG, "BEFORE COMPRESSION: " +
                    "Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    ". Max size: " + Util.byteToKB(rules.picture_max_size));

            file = PictureUtility.resize(file, rules.picture_max_width, rules.picture_max_height);
            MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));

            Log.d(TAG, "AFTER COMPRESSION: Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    " and type: " + fileMimeType);

            if (file.length() > rules.picture_max_size){
                throw new Exception("Picture size exceed limit: " + file.length() + "/" + rules.picture_max_size);
            }

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
                .onResponse(new FormErrorsCallback(getContext(), "Pictures"))
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

        } catch (Exception e) {
            Toast.makeText(this.getContext(), R.string.cannot_resize_picture, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            setUploadVisibility(false);
        }
    }

    // =============================================================================================

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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mPictureLoaderModel);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(mPictureLoaderModel);
        super.onStop();
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
    class PictureLoader extends DataLoader<Picture> {

        public PictureLoader(Context context) {
            super(context);
        }

        @Override
        protected Loader<List<Picture>> buildModelLoader() {
            return new AutoModelLoader(context, Picture.class, eventActivity.getEvent().getPicturesQuery(), false);
        }


        @Override
        public void onFinish(List<Picture> data) {
            //picturesAdapter.setBaseUrl(this.getServerResponse().extra.get("base_url"));
            picturesAdapter.setData(data);
            picturesAdapter.notifyDataSetChanged();
            noPicView.setVisibility(!picturesAdapter.hasData() ? View.VISIBLE : View.GONE);
        }


    }
}

