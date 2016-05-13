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
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.listeners.SelectableButtonListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;
import com.timappweb.timapp.views.EventView;
import com.timappweb.timapp.views.SelectableFloatingButton;
import com.timappweb.timapp.views.SpotView;

import java.util.List;
import java.util.Vector;

public class EventActivity extends BaseActivity {
    private static final int LOADER_PLACE_CORE = 0;
    private String TAG = "EventActivity";
    private MyPagerAdapter pagerAdapter;
    private Place event;
    private int eventId;

    //Views
    private View        iAmComingButton;
    private TextView    iAmComingTv;
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

    private AlphaAnimation postButtonsAppear;
    private AlphaAnimation postButtonsDisappear;
    private boolean firstLoad = true;


    //Override methods
    //////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        progressBottom = findViewById(R.id.progressview_bottom_place);
        onMyWayTv = (TextView) findViewById(R.id.text_onmyway_button);
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        progressView = findViewById(R.id.progress_view);
        postButtons = findViewById(R.id.event_post_buttons);
        matchButton = (SelectableFloatingButton) findViewById(R.id.fab);
        picButton = findViewById(R.id.event_post_pic);
        tagButton = findViewById(R.id.event_post_tags);
        peopleButton = findViewById(R.id.event_post_people);

        getSupportLoaderManager().initLoader(LOADER_PLACE_CORE, null, new EventLoader());
    }

    /**
     * Method called when event has finished loading.
     * @warning Method must be called only ONCE.
     */
    private void onEventLoaded() {
        if (firstLoad){
            firstLoad = false;
            initFragments();
            setListeners();
            setActions();

            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // TODO only listen for big location changed..
                    MyApplication.setLastLocation(location);
                    updateBtnVisibility();
                }
            };
            initLocationProvider(mLocationListener);
        }

        updateView();
    }

    public void setActions() {
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            switch (extras.getInt(IntentsUtils.KEY_ACTION, -1)) {
                case IntentsUtils.ACTION_CAMERA:
                    openAddPictureActivity();
                    //pager.setCurrentItem(0);
                    break;
                case IntentsUtils.ACTION_TAGS:
                    //pager.setCurrentItem(1);
                    openAddTagsActivity();
                    break;
                case IntentsUtils.ACTION_PEOPLE:
                    //pager.setCurrentItem(2);
                    openAddPeopleActivity();
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


    private void setListeners() {
        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPictureActivity();
                //pager.setCurrentItem(0);
            }
        });
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddTagsActivity();
            }
        });
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPeopleActivity();
            }
        });

        //Buttons appearance
        postButtonsAppear = new AlphaAnimation(0, 1);
        postButtonsAppear.setDuration(TIMELAPSE_BUTTONS_APPEAR_ANIM);

        //Buttons Disappearance
        postButtonsDisappear = new AlphaAnimation(1, 0);
        postButtonsDisappear.setDuration(TIMELAPSE_BUTTONS_DISAPPEAR_ANIM);

        matchButton.setSelectableListener(new SelectableButtonListener() {

            private void changeFabColor(int color) {
                matchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(EventActivity.this, color)));
            }

            public void setUIEnabled(int resource) {
                matchButton.setImageResource(resource);
                changeFabColor(R.color.colorPrimaryDark);
            }

            public void setUIDisabled(int resource) {
                matchButton.setImageResource(resource);
                changeFabColor(R.color.white);
            }

            @Override
            public void updateUI(boolean enabled) {
                //PlaceStatus placeStatus = PlaceStatusManager.getStatus(event);
                if (isUserAround()) {
                    //if (placeStatus != null && placeStatus.status == UserPlaceStatusEnum.HERE){
                    if (enabled) {
                        setUIEnabled(R.drawable.match_white);
                        postButtons.setVisibility(View.VISIBLE);
                    } else {
                        setUIDisabled(R.drawable.match_red);
                        postButtons.setVisibility(View.GONE);
                    }
                } else {
                    //if (placeStatus != null && placeStatus.status == UserPlaceStatusEnum.COMING){
                    if (enabled) {
                        setUIEnabled(R.drawable.ic_coming_guy_white);
                    } else {
                        setUIDisabled(R.drawable.ic_coming_guy_darkred);
                    }
                    postButtons.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean performEnabled() {
                addPlaceStatus();
                fragmentTags.getEventView().updatePointsView(true);
                postButtons.startAnimation(postButtonsAppear);
                return true;
            }

            @Override
            public boolean performDisabled() {
                removePlaceStatus();
                fragmentTags.getEventView().updatePointsView(false);
                postButtons.startAnimation(postButtonsDisappear);
                return true;
            }
        });
    }


    private void addPlaceStatus() {
        if (event == null){
            return;
        }
        // TODO fine location
        if (!MyApplication.hasFineLocation()) {
            Toast.makeText(this, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }
        final UserPlaceStatusEnum status = isUserAround() ? UserPlaceStatusEnum.HERE : UserPlaceStatusEnum.COMING;
        PlaceStatusManager.instance().add(this, event, status, new BinaryActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
                matchButton.setStateOn(false);
            }

            @Override
            public void onFinish() {
                matchButton.setEnabled(true);
            }

        });
    }
    private void removePlaceStatus() {
        if (event == null){
            return;
        }
        PlaceStatusManager.instance().cancel(this, event, new BinaryActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
                matchButton.setStateOn(true);
            }

            @Override
            public void onFinish() {
                matchButton.setEnabled(true);
            }
        });
    }

    private void openAddTagsActivity() {
        if (!MyApplication.hasFineLocation()) {
            Toast.makeText(this, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }
        IntentsUtils.addTags(this, event);
    }

    private void openAddPictureActivity() {
        IntentsUtils.addPictureFromFragment(this, fragmentPictures);
    }

    private void openAddPeopleActivity() {
        IntentsUtils.addPeople(this, event);
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



    /**
     *
     */
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

        PlaceStatus placeStatus = PlaceStatusManager.getStatus(event);
        boolean stateOn = placeStatus != null  && (placeStatus.status == UserPlaceStatusEnum.COMING || placeStatus.status == UserPlaceStatusEnum.HERE);
        matchButton.setStateOn(stateOn);

        updateBtnVisibility();
    }

    /**
     * Show or hide add post or coming button according to user location
     */
    public void updateBtnVisibility(){
        if (!MyApplication.isLoggedIn() || !MyApplication.hasLastLocation()) {
            matchButton.setVisibility(View.GONE);
            return;
        }
        matchButton.setVisibility(View.VISIBLE);
        matchButton.updateUI();
        pagerAdapter.getItem(pager.getCurrentItem()).setMenuVisibility(true);
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
            SyncBaseModel.getEntry(Place.class, EventActivity.this, eventId, DataSyncAdapter.SYNC_TYPE_PLACE);
            return new ModelLoader<>(EventActivity.this, Place.class, SyncBaseModel.queryByRemoteId(Place.class, eventId), false);
        }

        @Override
        public void onLoadFinished(Loader<List<Place>> loader, List<Place> data) {
            Log.d(TAG, "Place loaded finish");
            if (data.size() > 0){
                event = data.get(0);
                onEventLoaded();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Place>> loader) {

        }
    }

}