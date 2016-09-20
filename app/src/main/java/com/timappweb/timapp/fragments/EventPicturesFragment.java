package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.NetworkErrorCallback;
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
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import com.timappweb.timapp.views.SwipeRefreshLayout;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import retrofit2.Call;
import retrofit2.Response;


public class EventPicturesFragment extends EventBaseFragment implements
        OnTabSelectedListener,
        ActionMode.Callback{

    private static final String         TAG                             = "EventPicturesFragment";
    private static final long           REMOTE_LOAD_LIMIT               = 6;
    public static int                   PICUTRE_GRID_COLUMN_NB          = 2;
    private static final long           MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final long           MIN_DELAY_AUTO_REFRESH          = 10 * 60 * 1000;

    private static final int            INDEX_CONTEXTUAL_MENU_ITEM_SET_BACKGROUND = 0;
    // ---------------------------------------------------------------------------------------------

    private View                        noPicView;
    private View                        uploadView;
    private PicturesAdapter             picturesAdapter;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private RefreshableRecyclerView     mRecyclerView;
    private SectionDataLoader mDataLoader;
    private ActionModeHelper mActionModeHelper;

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        initVariables(root);

        initAdapter();
        initConfigEasyImage();
        initActionModeHelper(SelectableAdapter.MODE_SINGLE);
        return root;
    }

    private void initVariables(View root) {
        noPicView = root.findViewById(R.id.no_pictures_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), PICUTRE_GRID_COLUMN_NB));
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);
    }

    private void initConfigEasyImage() {
        EasyImage.configuration(eventActivity)
                .setImagesFolderName(getString(R.string.app_name))
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
    }

    private void initActionModeHelper(int mode) {
        //this = ActionMode.Callback instance
        mActionModeHelper = new ActionModeHelper(picturesAdapter, R.menu.menu_context_picture, this) {
                //Override to customize the title
                @Override
                public void updateContextTitle(int count) {

                }
            }
            .withDefaultMode(mode);
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
                    .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH);

        } catch (Exception e) {
            IntentsUtils.home(getContext());
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, eventActivity, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(eventActivity, R.string.error_camera, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                Log.d(TAG, "Result request camera");
                eventActivity.setCurrentPageSelected(EventActivity.PAGER_PICTURE);
                if (resultCode != Activity.RESULT_OK){
                    Log.e(TAG, "Activity result for requesting camera returned a non success code: " + resultCode);
                    return;
                }
                uploadPicture(imageFile);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(eventActivity);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
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
        picturesAdapter.initializeListeners(new FlexibleAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Log.d(TAG, "Long click on picture adapter item n°" + position);
                Picture item = picturesAdapter.getPicture(position);
                boolean currentUserOwnEvent = getEvent().isOwner(MyApplication.getCurrentUser());
                if (item != null && mActionModeHelper != null && currentUserOwnEvent){
                    mActionModeHelper.onLongClick(eventActivity, position);
                }
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
     * @param file
     */
    public void uploadPicture(File file) {
        if (uploadView.getVisibility() == View.VISIBLE){
            Toast.makeText(getContext(), R.string.upload_picture_in_progress, Toast.LENGTH_SHORT).show();
            return;
        }
        setUploadVisibility(true);

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
                this.showUploadFeedbackError(R.string.cannot_resize_picture);
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
                                R.string.no_network_access, Toast.LENGTH_LONG).show();
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

    private void setPictureAsEventBackground(final Picture picture) {
        // TODO JACK loader
        Map<String, String> params = new HashMap();
        params.put("picture_id", String.valueOf(picture.getRemoteId())); // TODO cst
        RestClient.buildCall(RestClient.service().setBackgroundPicture(getEvent().getRemoteId(), params))
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        getEvent().setBackgroundPicture(picture);
                        getEvent().mySaveSafeCall();
                        Toast.makeText(getActivity(), R.string.event_picture_updated, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void notSuccessful() {
                        Toast.makeText(getActivity(), R.string.action_performed_not_successful, Toast.LENGTH_LONG).show();
                    }
                })
                .onError(new NetworkErrorCallback(getContext()))
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        // TODO JACK hide loader here
                    }
                })
                .perform();
    }

    // =============================================================================================

    @Override
    public void onTabSelected() {
        mRecyclerView.smoothScrollToPosition(0);
    }


    /**
     * Get currently selected picture or null if nothing is selected
     * @return
     */
    public Picture getSelectedPicture(){
        List<Integer> selectedItems = picturesAdapter.getSelectedPositions();
        for (Integer position: selectedItems){
            Picture picture = picturesAdapter.getPicture(position);
            return picture;
        }
        return null;
    }
    // ---------------------------------------------------------------------------------------------



    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        eventActivity.hideActionBar();
        boolean currentUserOwnEvent = getEvent().isOwner(MyApplication.getCurrentUser());
        menu.getItem(INDEX_CONTEXTUAL_MENU_ITEM_SET_BACKGROUND)
                .setVisible(currentUserOwnEvent);

        picturesAdapter.setMode(SelectableAdapter.MODE_SINGLE);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_event_background:
                Picture picture = getSelectedPicture();
                if (picture != null)
                    setPictureAsEventBackground(picture);
                mActionModeHelper.destroyActionModeIfCan();
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        picturesAdapter.setMode(SelectableAdapter.MODE_IDLE);
        eventActivity.showActionBar();
    }

}

