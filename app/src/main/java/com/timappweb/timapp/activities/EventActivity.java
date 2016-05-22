package com.timappweb.timapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventPagerAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.fragments.EventInformationFragment;
import com.timappweb.timapp.fragments.EventPicturesFragment;
import com.timappweb.timapp.fragments.EventTagsFragment;
import com.timappweb.timapp.fragments.EventPeopleFragment;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.fragments.FragmentGroup;
import com.timappweb.timapp.utils.loaders.ModelLoader;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.EventView;
import com.timappweb.timapp.views.parallaxviewpager.ParallaxViewPagerBaseActivity;
import com.timappweb.timapp.views.parallaxviewpager.ScrollTabHolder;
import com.timappweb.timapp.views.slidingTab.SlidingTabLayout;

import java.util.List;

public class EventActivity extends ParallaxViewPagerBaseActivity implements LocationManager.LocationListener, ScrollTabHolder{

    private String          TAG                     = "EventActivity";

    private static final int INITIAL_FRAGMENT_PAGE  = 0;
    private static final int PAGER_OFFSCREEN_PAGE_LIMIT = 2;

    public static final int LOADER_ID_CORE          = 0;
    public static final int LOADER_ID_PICTURE       = 1;
    public static final int LOADER_ID_INVITATIONS   = 2;
    public static final int LOADER_ID_USERS         = 3;
    public static final int LOADER_ID_TAGS          = 4;

    private static final int REQUEST_CAMERA         = 0;

    // =============================================================================================

    private Event event;
    private int eventId;

    //Views
    private EventPicturesFragment fragmentPictures;
    private EventTagsFragment fragmentTags;
    private EventPeopleFragment fragmentPeople;

    //Listeners

    private boolean isEventLoaded = false;
    private EventView eventView;
    private SlidingTabLayout pagerTabStrip;
    private View mHeaderParallax;
    private EventInformationFragment fragmentInformation;
    private FragmentGroup mFragmentGroup;
    private View eventTitleContainer;
    private int windowHeight;
    private View mViewContainer;

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
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    Window window = getWindow();
        //    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //   window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorSecondaryDark));
        //}
/*
        FadingActionBarHelper helper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.background_button)
                .headerLayout(R.layout.header_event_activity)
                .contentLayout(R.layout.activity_event);
        setContentView(helper.createView(this));
        helper.initActionBar(this);*/

        setContentView(R.layout.activity_event);

        initToolbar(false);

        //Initialize
        //spotToolbar = (SpotView) findViewById(R.id.spot_view);
        //eventToolbar = (EventView) findViewById(R.id.event_view);
        //eventView = (EventView) findViewById(R.id.event_view);
        pagerTabStrip = (SlidingTabLayout) findViewById(R.id.pager_tab_strip);
        eventTitleContainer = findViewById(R.id.event_title_container);

        mHeader = findViewById(R.id.header);
        mHeaderParallax = findViewById(R.id.background_image_event);
        mViewContainer = findViewById(R.id.activity_event_container);

        //eventButtons = (EventButtonsView) findViewById(R.id.event_buttons_view);

        getSupportLoaderManager().initLoader(LOADER_ID_CORE, null, new EventLoader());
    }

    /**
     * Method called when event has finished loading.
     * @warning Method must be called only ONCE.
     */
    private void onEventLoaded() {
        if (!isEventLoaded){
            isEventLoaded = true;
            //eventView.setEvent(event);
            initFragments();
            initValues();
            setupAdapter();
            parseIntentParameters();
        }

        updateView();
    }

    public void parseIntentParameters() {
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            parseActionParameter(extras.getInt(IntentsUtils.KEY_ACTION, -1));
        }
    }

    public void parseActionParameter(int action){
        switch (action) {
            case IntentsUtils.ACTION_CAMERA:
                openAddPictureActivity();
                //mViewPager.setCurrentItem(0);
                break;
            case IntentsUtils.ACTION_TAGS:
                //mViewPager.setCurrentItem(1);
                openAddTagsActivity();
                break;
            case IntentsUtils.ACTION_PEOPLE:
                //mViewPager.setCurrentItem(2);
                openAddPeopleActivity();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.addOnLocationChangedListener(this);
        LocationManager.start(this);
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
                    mViewPager.setCurrentItem(1);
                    Log.d(TAG, "Result OK from TagActivity");
                }
                break;
            case IntentsUtils.REQUEST_INVITE_FRIENDS:
                if(resultCode==RESULT_OK) {
                    mViewPager.setCurrentItem(2);
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


    private void openAddTagsActivity() {
        if (!LocationManager.hasFineLocation()) {
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
        mFragmentGroup = FragmentGroup.createGroup(this);
        fragmentInformation = (EventInformationFragment) mFragmentGroup.add(Fragment.instantiate(this, EventInformationFragment.class.getName()));
        fragmentPictures = (EventPicturesFragment) mFragmentGroup.add(Fragment.instantiate(this, EventPicturesFragment.class.getName()));
        fragmentTags = (EventTagsFragment) mFragmentGroup.add(Fragment.instantiate(this, EventTagsFragment.class.getName()));
        fragmentPeople = (EventPeopleFragment) mFragmentGroup.add(Fragment.instantiate(this, EventPeopleFragment.class.getName()));

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        mFragmentAdapter = new EventPagerAdapter(super.getSupportFragmentManager(), mFragmentGroup.getFragments());
        mViewPager = (ViewPager) super.findViewById(R.id.event_viewpager);
        mViewPager.setOffscreenPageLimit(PAGER_OFFSCREEN_PAGE_LIMIT);
        mViewPager.setAdapter(this.mFragmentAdapter);
        mViewPager.setCurrentItem(INITIAL_FRAGMENT_PAGE);
        /*mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

    public void setmViewPager(int pageNumber) {
        mViewPager.setCurrentItem(pageNumber);
    }



    /**
     *
     */
    private void updateView() {
        try {
            EventCategory eventCategory = MyApplication.getCategoryById(event.category_id);
            //ImageView backgroundImage = (ImageView) findViewById(R.id.background_place);
            //backgroundImage.setImageResource(eventCategory.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + event.category_id);
        }
        /*
        if(event.spot==null) {
            eventToolbar.setVisibility(View.VISIBLE);
            spotToolbar.setVisibility(View.GONE);
            eventToolbar.setEvent(event);
        } else {
            spotToolbar.setVisibility(View.VISIBLE);
            eventToolbar.setVisibility(View.GONE);
            spotToolbar.setSpot(event.spot);
        }*/
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

    public Event getEvent() {
        return event;
    }

    public int getEventId() {
        return eventId;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isUserAround() {
        return this.event != null && this.event.isUserAround();
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        if (isEventLoaded) {
            mFragmentAdapter.getItem(mViewPager.getCurrentItem()).setMenuVisibility(true);
        }
    }


    // =============================================================================================
    // SCROLL TAB

    @Override
    protected void initValues() {
        //int tabHeight = pagerTabStrip.getHeight() + eventTitleContainer.getHeight() + mToolbar.getHeight();
        mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mMinHeaderHeight;
        mNumFragments = mFragmentGroup.size();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowHeight = size.y;
    }


    @Override
    protected void scrollHeader(int scrollY) {
        float translationY = Math.max(-scrollY, mMinHeaderTranslation);
        mHeader.setTranslationY(translationY);
        mHeaderParallax.setTranslationY(-translationY/3);

       // mViewPager.getLayoutParams().height = (int)(windowHeight - mHeader.getHeight());
        //mViewPager.requestLayout();

        mHeader.requestLayout();
        mViewContainer.requestLayout();
        mViewPager.requestLayout();
    }

    @Override
    protected void setupAdapter() {
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(mNumFragments);
        pagerTabStrip.setOnPageChangeListener(getViewPagerChangeListener());
        pagerTabStrip.setViewPager(mViewPager);
    }

    // =============================================================================================

    class EventLoader implements LoaderManager.LoaderCallbacks<List<Event>>{

        @Override
        public Loader<List<Event>> onCreateLoader(int id, Bundle args) {
            SyncBaseModel.getEntry(Event.class, EventActivity.this, eventId, DataSyncAdapter.SYNC_TYPE_EVENT);
            return new ModelLoader<>(EventActivity.this, Event.class, SyncBaseModel.queryByRemoteId(Event.class, eventId), false);
        }

        @Override
        public void onLoadFinished(Loader<List<Event>> loader, List<Event> data) {
            Log.d(TAG, "Event loaded finish");
            if (data.size() > 0){
                event = data.get(0);
                if (!event.hasLocalId()) event.mySave();
                onEventLoaded();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Event>> loader) {

        }
    }

}