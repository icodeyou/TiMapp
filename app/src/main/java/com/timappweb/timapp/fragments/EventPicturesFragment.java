package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.PicturesAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.PictureItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RetryOnErrorCallback;
import com.timappweb.timapp.rest.io.serializers.AddPictureMapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.PictureInterface;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import okhttp3.RequestBody;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import retrofit2.Call;
import retrofit2.Response;

public class EventPicturesFragment extends EventBaseFragment implements OnTabSelectedListener,
        ActionMode.Callback{

    private static final String         TAG                             = "EventPicturesFragment";
    private static final long           REMOTE_LOAD_LIMIT               = 10;
    private static final long           CACHE_DURATION = 3600 * 1000;
    public static int                   PICTURE_GRID_COLUMN_NB          = 2;
    private static final long           MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final long           MIN_DELAY_AUTO_REFRESH          = 5 * 60 * 1000;

    private static final int            INDEX_CONTEXTUAL_MENU_ITEM_SET_BACKGROUND = 0;
    // ---------------------------------------------------------------------------------------------

    private View                        noPicView;
    private View                        uploadView;
    private PicturesAdapter             picturesAdapter;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private RefreshableRecyclerView     mRecyclerView;
    private View                        bottomSheet;
    private TextView                    textInfoPic;

    private BottomSheetBehavior<View> bottomSheetBehaviour;
    private CursorPaginateDataLoader<Picture, Picture> mDataLoader;
    private ActionModeHelper mActionModeHelper;
    private int lastPositionSelected = -1;
    private CursorPaginateManager<Picture> paginatorManager;

    public EventPicturesFragment() {
        setTitle(R.string.title_fragment_pictures);
    }

    // ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_pictures, container, false);
        initVariables(root);

        bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehaviour.setPeekHeight(0);

        initAdapter();
        initConfigEasyImage();
        initActionModeHelper(SelectableAdapter.MODE_SINGLE);

        return root;
    }


    private void initVariables(View root) {
        noPicView = root.findViewById(R.id.no_pictures_view);
        uploadView = root.findViewById(R.id.upload_view);
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.pictures_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), PICTURE_GRID_COLUMN_NB));
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_picture);

        bottomSheet = root.findViewById( R.id.bottom_sheet);
        textInfoPic = (TextView) root.findViewById(R.id.text_info_pic);
    }

    private void initConfigEasyImage() {
        EasyImage.configuration(eventActivity)
                .setImagesFolderName(getString(R.string.app_name))
                .saveInRootPicturesDirectory()
                .setCopyExistingPicturesToPublicLocation(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        try {
            if (eventActivity.getEvent() == null){
                throw new Exception("Cannot add picture for null event");
            }
            Event event = (Event) eventActivity.getEvent().requireLocalId();
            eventActivity.setEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
            eventActivity.exit();
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
        picturesAdapter = new PicturesAdapter(getActivity(), PICTURE_GRID_COLUMN_NB);
        mRecyclerView.setAdapter(picturesAdapter);

        picturesAdapter.setAutoScrollOnExpand(true);
        picturesAdapter.setHandleDragEnabled(true);
        picturesAdapter.setAnimationOnScrolling(true);
        picturesAdapter.setAnimationOnReverseScrolling(true);
        picturesAdapter.initializeListeners(new FlexibleAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                Log.d(TAG, "Long click on picture adapter item n°" + position);
                selectPicUI(position);
            }
        });
        picturesAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                Log.d(TAG, "Clicking on picture adapter item n°" + position);
                bottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                if(mActionModeHelper.getActionMode() == null) {
                    AbstractFlexibleItem item = picturesAdapter.getItem(position);
                    if(item instanceof PictureItem) {
                        IntentsUtils.viewPicture(EventPicturesFragment.this.getActivity(),
                                (position - picturesAdapter.getGridColumnNumber()),
                                picturesAdapter.getPictureUris());
                        return true;
                    }
                    return false;
                }
                else {
                    if(lastPositionSelected != -1 && lastPositionSelected == position) {
                        mActionModeHelper.destroyActionModeIfCan();
                    }
                    else {
                        selectPicUI(position);
                    }
                    return true;
                }
            }
        });
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                //NOTE: This allows to receive Payload objects when notifyItemChanged is called by the Adapter!!!
                return true;
            }
        });
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    private void selectPicUI(int position) {
        Picture item = picturesAdapter.getPicture(position);
        if (item != null && mActionModeHelper != null){
            mActionModeHelper.onLongClick(eventActivity, position);
            textInfoPic.setText(item.getTimeCreated());
        }
        lastPositionSelected = position;
    }

    private void initActionModeHelper(int mode) {
        mActionModeHelper = new ActionModeHelper(picturesAdapter, R.menu.menu_context_picture, this) {
            //Override to customize the title
            @Override
            public void updateContextTitle(int count) {
                mActionMode.setTitle(getContext().getString(R.string.selection));
            }
        }
                .withDefaultMode(mode);
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
    public void uploadPicture(@NonNull File file) {
        if (uploadView.getVisibility() == View.VISIBLE){
            Toast.makeText(getContext(), R.string.upload_picture_in_progress, Toast.LENGTH_SHORT).show();
            return;
        }
        setUploadVisibility(true);

        try {
            RequestBody body = new AddPictureMapper(file).compress().build();

            final Event event = eventActivity.getEvent();
            final Picture picture = new Picture();
            picture.setEvent(event);
            picture.setUser(MyApplication.getCurrentUser());

            Call call = RestClient.instance().createService(PictureInterface.class).upload(event.getRemoteId(), body);
            HttpCallManager callManager = RestClient.buildCall(call);
            callManager
                .beforeStart(new HttpCallManager.BeforeStartCallback(){
                    @Override
                    public void onBeforeStart() {
                        setUploadVisibility(true);
                    }
                })
                .onResponse(new AutoMergeCallback(picture))
                .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser(), QuotaType.ADD_PICTURE))
                .onResponse(new FormErrorsCallback(getContext(), "Pictures"))
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        // Get the bitmap in according to the width of the device
                        mRecyclerView.smoothScrollToPosition(0);
                        picture.mySaveSafeCall();
                        paginatorManager.refresh();
                        Toast.makeText(EventPicturesFragment.this.getContext(),
                                R.string.thanks_for_add_picture, Toast.LENGTH_LONG).show();
                    }

                })
                .onError(new RetryOnErrorCallback(getActivity(), callManager))
                .onFinally(new HttpCallManager.FinallyCallback() {

                    @Override
                    public void onFinally(Response response, Throwable error) {
                        setUploadVisibility(false);
                    }
                })
                .perform();

        }
        catch (AddPictureMapper.CannotUploadPictureException e) {
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath() + ". " + e.getMessage());
            this.showUploadFeedbackError(e.getResId());
            if (BuildConfig.DEBUG){
                e.printStackTrace();
            }
        }
    }

    private void showUploadFeedbackError(int msg){
        Toast.makeText(this.getContext(), msg, Toast.LENGTH_LONG).show();
        setUploadVisibility(false);
    }

    private void setPictureAsEventBackground(final Picture picture) {
        Map<String, String> params = new HashMap();
        params.put("picture_id", String.valueOf(picture.getRemoteId())); // TODO cst
        RestClient.buildCall(RestClient.service().setBackgroundPicture(getEvent().getRemoteId(), params))
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        getEvent().setBackgroundPicture(picture);
                        getEvent().savePicture();
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
        if(mRecyclerView!=null) {
            mRecyclerView.smoothScrollToPosition(0);
        }

        loadDataIfNeeded();
    }

    private void loadDataIfNeeded() {
        if (this.mDataLoader != null || !isAdded()) return;
        mDataLoader = CursorPaginateDataLoader.<Picture, Picture>create(
                        "pictures/event/" + getEvent().getRemoteId(),
                        Picture.class)
                .initCache("EventPicture" + eventActivity.getEvent().getRemoteId(), CACHE_DURATION)
                .setLocalQuery(new Select().from(Picture.class).where("Event = ?", getEvent().getId()))
                //.setLimit(8)
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<Picture, Picture>() {
                    @Override
                    public Picture beforeSaveModel(Picture model) {
                        model.event = getEvent();
                        return model;
                    }
                })
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createCreatedFilter())
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createSyncIdFilter());
        paginatorManager = new CursorPaginateManager<Picture>(getContext(), picturesAdapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<Picture>(){
                    @Override
                    public AbstractFlexibleItem createItem(Picture data) {
                        return new PictureItem(data);
                    }
                })
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .enableEndlessScroll()
                .setNoDataView(noPicView)
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .load();
    }


    @Override
    public void onTabUnselected() {
        if(mActionModeHelper != null) {
            mActionModeHelper.destroyActionModeIfCan();
        }
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
        boolean currentUserOwnEvent = getEvent().isOwner(MyApplication.getCurrentUser());
        menu.getItem(INDEX_CONTEXTUAL_MENU_ITEM_SET_BACKGROUND)
                .setVisible(currentUserOwnEvent);
        bottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        lastPositionSelected = -1;
        bottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}

