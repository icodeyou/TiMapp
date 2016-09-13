package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.timappweb.timapp.adapters.flexibleadataper.models.PictureItem;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.DBCacheEngine;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.sections.SectionRecyclerViewManager;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionDataProviderInterface;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.PictureInterface;
import com.timappweb.timapp.sync.callbacks.PictureSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import com.timappweb.timapp.views.SwipeRefreshLayout;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class EventPicturesFragment extends EventBaseFragment implements
        OnTabSelectedListener{

    private static final String         TAG                             = "EventPicturesFragment";
    private static final long           REMOTE_LOAD_LIMIT               = 6;
    public static int                   PICUTRE_GRID_COLUMN_NB          = 2;
    private static final long           MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final long           MIN_DELAY_AUTO_REFRESH          = 10 * 60 * 1000;

    // ---------------------------------------------------------------------------------------------

    private View                        noPicView;
    private View                        uploadView;
    private PicturesAdapter             picturesAdapter;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private RefreshableRecyclerView     mRecyclerView;
    private SectionDataLoader mDataLoader;

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        initVariables(root);

        initAdapter();

        return root;
    }

    private void initVariables(View root) {
        noPicView = root.findViewById(R.id.no_pictures_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), PICUTRE_GRID_COLUMN_NB));
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            if (eventActivity.getEvent() == null){
                // TODO ???
                throw new Exception("Cannot add picture for null event");
            }
            eventActivity.setEvent((Event) eventActivity.getEvent().requireLocalId());

            initDataLoader();

            new SectionRecyclerViewManager(getContext(), picturesAdapter, mDataLoader)
                    .setItemTransformer(new RecyclerViewManager.ItemTransformer<Picture>(){
                        @Override
                        public AbstractFlexibleItem createItem(Picture data) {
                            return new PictureItem(data);
                        }
                    })
                    .setNoDataView(noPicView)
                    .setSwipeRefreshLayout(mSwipeRefreshLayout)
                    .enableEndlessScroll()
                    .setMinDelayAutoRefresh(MIN_DELAY_AUTO_REFRESH)
                    .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                    .firstLoad();

        } catch (Exception e) {
            IntentsUtils.home(getContext());
        }
        super.onViewCreated(view, savedInstanceState);
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
        picturesAdapter.setAutoScrollOnExpand(true);
        picturesAdapter.setHandleDragEnabled(true);
        picturesAdapter.setAnimationOnScrolling(true);
        picturesAdapter.setAnimationOnReverseScrolling(true);
        picturesAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                Log.d(TAG, "Clicking on picture adapter item n°" + position);
                AbstractFlexibleItem item = picturesAdapter.getItem(position);
                if (item instanceof PictureItem){
                    IntentsUtils.viewPicture(EventPicturesFragment.this.getActivity(),
                            (position - picturesAdapter.getGridColumnNumber()),
                            picturesAdapter.getPictureUris());
                    return true;
                }
                return false;
            }
        });
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
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    private void initDataLoader() {
        mDataLoader = new SectionDataLoader<Picture>()
                .setFormatter(SyncBaseModel.getPaginatedFormater())
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayRefresh(MIN_DELAY_FORCE_REFRESH)
                .setCacheEngine(new DBCacheEngine<Picture>(Picture.class){
                    @Override
                    protected String getHashKey() {
                        return "EventPicture" + getEvent().getRemoteId();
                    }

                    @Override
                    protected void persist(List<Picture> data) throws Exception {
                        new MultipleEntriesSyncPerformer<Picture, ResponseSyncWrapper<Picture>>()
                                .setRemoteEntries(data)
                                .setLocalEntries(getEvent().getPictures())
                                .setCallback(new PictureSyncCallback(getEvent()))
                                .perform();
                    }
                })
                .useCache(false)
                .setDataProvider(new SectionDataProviderInterface() {

                    @Override
                    public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                        RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(REMOTE_LOAD_LIMIT);
                        return RestClient.buildCall(RestClient.service().viewPicturesForPlace(getEvent().getRemoteId(), options.toMap()));
                    }

                });

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

            // TODO
            //file = PictureUtility.resize(file, rules.picture_max_width, rules.picture_max_height);
            MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));

            Log.d(TAG, "AFTER COMPRESSION: Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    " and type: " + fileMimeType);

            if (file.length() > rules.picture_max_size){
                this.showUploadFeedbackError(R.string.error_picture_too_big);
                return;
            }
            else if (file.length() <= rules.picture_min_size){
                this.showUploadFeedbackError(R.string.error_picture_too_small);
                return;
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
                        mDataLoader.loadNewest();
                        Toast.makeText(EventPicturesFragment.this.getContext(),
                                R.string.thanks_for_add_picture, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void notSuccessful() {
                        if (this.response.code() != HttpURLConnection.HTTP_BAD_REQUEST){
                            Log.e(TAG, "Cannot upload picture. API response: " + this.response.code());
                            Toast.makeText(getContext(), R.string.cannot_upload_picture, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .onError(new RequestFailureCallback(){
                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(EventPicturesFragment.this.getContext(),
                                R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        setUploadVisibility(false);
                    }
                })
                .perform();

        }
        catch (Exception e) {
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            this.showUploadFeedbackError(R.string.cannot_resize_picture);
        }
    }

    private void showUploadFeedbackError(int msg){
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_LONG).show();
        setUploadVisibility(false);
    }

    // =============================================================================================

    @Override
    public void onTabSelected() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    // ---------------------------------------------------------------------------------------------


}

