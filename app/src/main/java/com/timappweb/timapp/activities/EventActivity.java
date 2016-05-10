package com.timappweb.timapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.PlaceStatus;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.fragments.PlaceBaseFragment;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlacePicturesFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.listeners.SelectableButtonListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;
import com.timappweb.timapp.views.EventView;
import com.timappweb.timapp.views.SelectableFloatingButton;
import com.timappweb.timapp.views.SpotView;

import java.util.List;
import java.util.Vector;

import retrofit2.Call;

public class EventActivity extends BaseActivity {
    private String TAG = "EventActivity";
    private MyPagerAdapter pagerAdapter;
    private Place event;
    private int eventId;
    private Activity currentActivity;

    //Views
    private View        iAmComingButton;
    private TextView    iAmComingTv;
    private View        onMyWayButton;
    private TextView    onMyWayTv;
    private View        progressView;
    private ListView    tagsListView;
    private EventView   eventToolbar;
    private SpotView    spotToolbar;
    private View        progressBottom;
    private View        parentLayout;
    private View        postButtons;
    private SelectableFloatingButton matchButton;
    private View picButton;
    private View tagButton;
    private View peopleButton;

    //Static variables
    private static final int REQUEST_CAMERA = 0;
    private static final int TIMELAPSE_BUTTONS_APPEAR_ANIM = 800;
    private static final int TIMELAPSE_BUTTONS_DISAPPEAR_ANIM = 300;

    private PlacePicturesFragment fragmentPictures;
    private PlaceTagsFragment fragmentTags;
    private PlacePeopleFragment fragmentPeople;
    private ViewPager pager;

    private ShareActionProvider shareActionProvider;
    private int counter = 0;

    private LocationListener mLocationListener;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;


    //Listeners
    private View.OnClickListener tagListener;
    private View.OnClickListener pictureListener;
    private View.OnClickListener peopleListener;
    private Vector<PlaceBaseFragment> childFragments;

    private boolean isMatchButtonSelected;
    private AlphaAnimation postButtonsAppear;
    private AlphaAnimation postButtonsDisappear;
    private SelectableButtonListener selectableButtonListener;


    //Override methods
    //////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        this.event = IntentsUtils.extractPlace(getIntent());
        eventId = IntentsUtils.extractPlaceId(getIntent());
        if (event == null && eventId <= 0){
            Log.e(TAG, "Trying to view an invalid event --> redirect to home");
            IntentsUtils.home(this);
            return;
        }
        else if (eventId <= 0){
            eventId = event.remote_id;
        }

        //Set status bar blue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorSecondaryDark));
        }

        setContentView(R.layout.activity_event);
        int colorRes = ContextCompat.getColor(this, R.color.white);
        initToolbar(false, colorRes);

        //Initialize
        spotToolbar = (SpotView) findViewById(R.id.spot_view);
        eventToolbar = (EventView) findViewById(R.id.event_view);
        parentLayout = findViewById(R.id.main_layout_place);
        iAmComingButton = findViewById(R.id.button_coming);
        iAmComingTv = (TextView) findViewById(R.id.text_coming_button);
        onMyWayButton = findViewById(R.id.button_on_my_way);
        progressBottom = findViewById(R.id.progressview_bottom_place);
        onMyWayTv = (TextView) findViewById(R.id.text_onmyway_button);
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        progressView = findViewById(R.id.progress_view);
        postButtons = findViewById(R.id.event_post_buttons);
        matchButton = (SelectableFloatingButton) findViewById(R.id.fab);
        picButton = findViewById(R.id.event_post_pic);
        tagButton = findViewById(R.id.event_post_tags);
        peopleButton = findViewById(R.id.event_post_people);

        initLocationListener();
        initFragments();
        setListeners();
        setActions();

        EventLoader mLoader = new EventLoader();
        getSupportLoaderManager().initLoader(0, null, mLoader);
    }

    public void setActions() {
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            switch (extras.getInt(IntentsUtils.KEY_ACTION, -1)) {
                case IntentsUtils.ACTION_CAMERA:
                    //pager.setCurrentItem(0);
                    break;
                case IntentsUtils.ACTION_TAGS:
                    //pager.setCurrentItem(1);
                    addTags();
                    break;
                case IntentsUtils.ACTION_PEOPLE:
                    //pager.setCurrentItem(2);
                    IntentsUtils.addPeople(this, event);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_share:
                setDefaultShareIntent();
                return true;
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case IntentsUtils.REQUEST_TAGS:
                if(resultCode==RESULT_OK) {
                    pager.setCurrentItem(1);
                    Log.d(TAG, "Result OK from TagActivity");
                }
                break;
            case IntentsUtils.REQUEST_INVITE_FRIENDS:
                if(resultCode==RESULT_OK) {
                    pager.setCurrentItem(2);
                    Log.d(TAG, "Result OK from InviteFriendsActivity");
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //private methods
    //////////////////////////////////////////////////////////////////////////////

    private void reloadData() {
        invalidateOptionsMenu();
        for (PlaceBaseFragment fragment: childFragments){
            fragment.loadData();
        }
        updateBtnVisibility();
    }

    private void changeFabColor(int color) {
        matchButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, color)));
    }


    private void setListeners() {
        final Activity eventActivity = this;

        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPictureFromFragment(eventActivity, fragmentPictures);
                //pager.setCurrentItem(0);
            }
        });
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTags();
                //pager.setCurrentItem(1);
            }
        });
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPeople(eventActivity, event);
                //pager.setCurrentItem(2);
            }
        });

        //Buttons appearance
        postButtonsAppear = new AlphaAnimation(0, 1);
        postButtonsAppear.setDuration(TIMELAPSE_BUTTONS_APPEAR_ANIM);

        //Buttons Disappearance
        postButtonsDisappear = new AlphaAnimation(1, 0);
        postButtonsDisappear.setDuration(TIMELAPSE_BUTTONS_DISAPPEAR_ANIM);


        selectableButtonListener = new SelectableButtonListener() {
            @Override
            public void onAble() {
                changeFabColor(R.color.colorPrimaryDark);
                fragmentTags.getEventView().updatePointsView(true);
                if (isUserAround()) {
                    matchButton.setImageResource(R.drawable.match_white);
                    postButtons.startAnimation(postButtonsAppear);
                    postButtons.setVisibility(View.VISIBLE);
                } else {
                    matchButton.setImageResource(R.drawable.ic_coming_guy_white);
                    addComing();
                }
            }

            @Override
            public void onDisable() {
                changeFabColor(R.color.white);
                fragmentTags.getEventView().updatePointsView(false);
                if(isUserAround()) {
                    matchButton.setImageResource(R.drawable.match_red);
                    postButtons.startAnimation(postButtonsDisappear);
                    postButtons.setVisibility(View.GONE);
                } else {
                    matchButton.setImageResource(R.drawable.ic_coming_guy_darkred);
                }
            }
        };

        matchButton.setSelectableListener(selectableButtonListener);
        matchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matchButton.switchButton();
            }
        });
    }

    private void addComing() {
        // TODO fine location
        if (!MyApplication.hasLastLocation()) {
            Toast.makeText(currentActivity, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }
        QueryCondition conditions = new QueryCondition();
        conditions.setPlaceId(eventId);
        conditions.setAnonymous(false);
        conditions.setUserLocation(MyApplication.getLastLocation());

        Call<RestFeedback> call = RestClient.service().notifyPlaceComing(conditions.toMap());
        call.enqueue(new RestFeedbackCallback(currentActivity) {
            @Override
            public void onActionSuccess(RestFeedback feedback) {
                Log.d(TAG, "Success register coming for user on place " + eventId);
                PlaceStatusManager.add(eventId, UserPlaceStatusEnum.COMING);
            }

            @Override
            public void onActionFail(RestFeedback feedback) {
                Log.d(TAG, "Fail register coming for user on event " + eventId);
                Toast.makeText(EventActivity.this,
                        getString(R.string.cannot_add_coming_status), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTags() {
        // TODO fine location
        if (!MyApplication.hasLastLocation()) {
            Toast.makeText(currentActivity, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }

        QueryCondition conditions = new QueryCondition();
        conditions.setPlaceId(event.remote_id);
        conditions.setAnonymous(false);
        conditions.setUserLocation(MyApplication.getLastLocation());
        Call<RestFeedback> call = RestClient.service().notifyPlaceHere(conditions.toMap());
        call.enqueue(new RestFeedbackCallback(currentActivity) {
            @Override
            public void onActionSuccess(RestFeedback feedback) {
                Log.d(TAG, "Success register here for user");
            }

            @Override
            public void onActionFail(RestFeedback feedback) {
                Log.d(TAG, "Fail register here for user");
            }
        });

        IntentsUtils.addTags(currentActivity, event);
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        childFragments = new Vector();

        Bundle bundle = new Bundle();
        fragmentPictures = (PlacePicturesFragment) Fragment.instantiate(this, PlacePicturesFragment.class.getName());
        fragmentTags = (PlaceTagsFragment) Fragment.instantiate(this, PlaceTagsFragment.class.getName());
        fragmentPeople = (PlacePeopleFragment) Fragment.instantiate(this, PlacePeopleFragment.class.getName());

        // Ajout des Fragments dans la liste
        childFragments.add(fragmentPictures);
        childFragments.add(fragmentTags);
        childFragments.add(fragmentPeople);

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), childFragments);
        pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        pager.setOffscreenPageLimit(2);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);
        pager.setCurrentItem(1);
        /*pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateBtnVisibility();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/
    }

    public void setPager(int pageNumber) {
        pager.setCurrentItem(pageNumber);
    }

    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MyApplication.setLastLocation(location);
                if (MyApplication.hasFineLocation()){
                    updateBtnVisibility();
                }
            }
        };
        initLocationProvider(mLocationListener);
    }


    private void updateView() {
        progressView.setVisibility(View.GONE);
        try {
            EventCategory eventCategory = MyApplication.getCategoryById(event.category_id);
            ImageView backgroundImage = (ImageView) findViewById(R.id.background_place);
            backgroundImage.setImageResource(eventCategory.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + event.category_id);
        }
        if(event.spot==null) {
            eventToolbar.setVisibility(View.VISIBLE);
            spotToolbar.setVisibility(View.GONE);
            eventToolbar.setEvent(event);
        } else {
            spotToolbar.setVisibility(View.VISIBLE);
            eventToolbar.setVisibility(View.GONE);
            spotToolbar.setSpot(event.spot);
        }
        updateBtnVisibility();
    }

    /**
     * Show or hide add post or coming button according to user location
     */
    public void updateBtnVisibility(){
        if(fragmentTags.getEventView()!=null && MyApplication.hasLastLocation()) {

            //if we are in the place
            boolean isUserComing = PlaceStatus.hasStatus(eventId, UserPlaceStatusEnum.COMING);
            boolean isAllowedToAddComing = !isUserComing && QuotaManager.instance().checkQuota(QuotaType.NOTIFY_COMING);
            //boolean isAllowedToCome = !isUserAround() && isAllowedToAddComing;

            boolean isHereStatus = PlaceStatus.hasStatus(eventId, UserPlaceStatusEnum.HERE);; //TODO : Fetch user real status
            boolean isAllowedToAddHere = true; //TODO : Fetch user real allowance

            matchButton.setVisibility(View.GONE);

            if(isUserAround()) {
                matchButton.setVisibility(View.VISIBLE);
                matchButton.setState(isHereStatus);
            } else {
                if(isAllowedToAddComing || isUserComing) {
                    matchButton.setVisibility(View.VISIBLE);
                    matchButton.setState(isUserComing);
                }
            }

            //iAmComingButton.setVisibility(progressView.getVisibility() != View.VISIBLE && isAllowedToCome ? View.VISIBLE : View.GONE);
            //onMyWayButton.setVisibility(event != null && !event.isAround() && progressView.getVisibility() != View.VISIBLE
            //        && isUserComing ? View.VISIBLE : View.GONE);
            pagerAdapter.getItem(pager.getCurrentItem()).setMenuVisibility(true);
        }
    }

    private void setDefaultShareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_place_text));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    //Public methods
    //////////////////////////////////////////////////////////////////////////////


    // Check for camera permission in MashMallow
    public void requestForCameraPermission() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show permission rationale
            } else {
                // Handle the result in UserActivity#onRequestPermissionResult(int, String[], int[])
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CAMERA);
            }
        } else {
            // Start CameraActivity
            IntentsUtils.addPicture(this);
        }
    }

    public Place getEvent() {
        return event;
    }

    public EventView getEventToolbar() {
        return eventToolbar;
    }

    public SpotView getSpotToolbar() {
        return spotToolbar;
    }

    public int getEventId() {
        return eventId;
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isUserAround() {
        return this.event != null && this.event.isAround();
    }



    // =============================================================================================

    class EventLoader implements LoaderManager.LoaderCallbacks<List<Place>>{

        @Override
        public Loader<List<Place>> onCreateLoader(int id, Bundle args) {
            event = (Place) SyncBaseModel.getEntry(Place.class, EventActivity.this, eventId, DataSyncAdapter.SYNC_TYPE_PLACE);
            if (event != null){
                updateView();
            }
            return new ModelLoader<>(EventActivity.this, Place.class, SyncBaseModel.queryByRemoteId(Place.class, eventId), false);
        }

        @Override
        public void onLoadFinished(Loader<List<Place>> loader, List<Place> data) {
            Log.d(TAG, "Place loaded finish");
            if (data.size() > 0){
                event = data.get(0);
                updateView();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Place>> loader) {

        }
    }

}