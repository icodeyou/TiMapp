package com.timappweb.timapp.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventPagerAdapter;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.fragments.EventBaseFragment;
import com.timappweb.timapp.fragments.EventInformationFragment;
import com.timappweb.timapp.fragments.EventPeopleFragment;
import com.timappweb.timapp.fragments.EventPicturesFragment;
import com.timappweb.timapp.fragments.EventTagsFragment;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RetryOnErrorCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.fragments.FragmentGroup;
import com.timappweb.timapp.utils.location.LocationManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import retrofit2.Call;
import retrofit2.Response;

public class EventActivity extends BaseActivity implements LocationManager.LocationListener{

    private static final long MIN_DELAY_UPDATE_EVENT = 5 * 60 * 1000;
    private String          TAG                     = "EventActivity";

    public static final int PAGER_INFO             = 0;
    public static final int PAGER_PICTURE           = 1;
    public static final int PAGER_TAG              = 2;
    public static final int PAGER_PEOPLE           = 3;
    public static final int INITIAL_FRAGMENT_PAGE  = 0;


    private static final int PAGER_OFFSCREEN_PAGE_LIMIT = 4;

    public static final int LOADER_ID_CORE          = 0;
    public static final int LOADER_ID_PICTURE       = 1;
    public static final int LOADER_ID_INVITATIONS   = 2;
    public static final int LOADER_ID_USERS         = 3;
    public static final int LOADER_ID_TAGS          = 4;

    private static final int REQUEST_CAMERA         = 0;

    // ---------------------------------------------------------------------------------------------

    private Event                       event;
    private long                        eventId;

    private EventPicturesFragment       fragmentPictures;
    private EventTagsFragment           fragmentTags;
    private EventPeopleFragment         fragmentPeople;
    private EventInformationFragment    fragmentInformation;

    private boolean                     isEventLoaded               = false;
    private FragmentGroup<EventBaseFragment>               mFragmentGroup;
    private MaterialViewPager           mMaterialViewPager;
    private EventPagerAdapter           mFragmentAdapter;
    private TextView                    pageTitle;
    private ViewDataBinding             mBinding;
    private FloatingActionButton        btnActionCamera;
    private FloatingActionButton        btnActionTag;
    private FloatingActionButton        btnActionInvite;
    private View                        loader;
    private FloatingActionsMenu btnAction;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mBinding = DataBindingUtil.setContentView(this, R.layout.activity_event);
            loader = findViewById(R.id.progress_view);
            pageTitle = (TextView) findViewById(R.id.title_event);
            btnAction = (FloatingActionsMenu)findViewById(R.id.multiple_actions);
            btnActionCamera = (FloatingActionButton) findViewById(R.id.action_camera);
            btnActionTag = (FloatingActionButton) findViewById(R.id.action_tag);
            btnActionInvite = (FloatingActionButton) findViewById(R.id.action_invite);

            initListeners();
            this.loadEvent();
        } catch (CannotSaveModelException e) {
            e.printStackTrace();
            this.exit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                setDefaultShareIntent();
                return true;
            case android.R.id.home:
                //http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity

                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
        LocationManager.addOnLocationChangedListener(this);
    }

    @Override
    protected void onStop() {
        LocationManager.removeLocationListener(this);
        LocationManager.stop(this);
        super.onStop();
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
                    setCurrentPageSelected(PAGER_TAG);
                    Log.d(TAG, "Result OK from AddTagActivity");
                }
                break;
            case IntentsUtils.REQUEST_INVITE_FRIENDS:
                if(resultCode==RESULT_OK) {
                    setCurrentPageSelected(PAGER_PEOPLE);
                    Log.d(TAG, "Result OK from InviteFriendsActivity");
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(this.isTaskRoot()) {
            IntentsUtils.home(this);
        } else {
            super.onBackPressed();
        }
    }


    //Methods
    //////////////////////////////////////////////////////////////////////////////

    private void exit(){
        IntentsUtils.home(this);
        finish();
    }

    private void loadEvent() throws CannotSaveModelException {
        this.event = IntentsUtils.extractEvent(getIntent());
        eventId = IntentsUtils.extractPlaceId(getIntent());
        if (event == null && eventId <= 0){
            Log.e(TAG, "Trying to view an invalid event --> redirect to home");
            this.exit();
            return;
        }
        else if (eventId <= 0){
            eventId = event.remote_id;
        }

        if (event == null || SyncHistory.requireUpdate(DataSyncAdapter.SYNC_TYPE_EVENT, event, MIN_DELAY_UPDATE_EVENT)){
            loader.setVisibility(View.VISIBLE);
            Call<Event> call = RestClient.service().viewPlace(eventId);
            Log.i(TAG, "Loading event with id: " + eventId + ". Existing event: " + event);
            RestClient.buildCall(call)
                    .onResponse(new HttpCallback<Event>() {
                        @Override
                        public void successful(Event event) {
                            try {
                                EventActivity.this.event = event.deepSave();
                                SyncHistory.updateSync(DataSyncAdapter.SYNC_TYPE_EVENT, EventActivity.this.event);
                                onEventLoaded();
                            } catch (CannotSaveModelException e) {
                                e.printStackTrace();
                                EventActivity.this.exit();
                            }
                        }

                        @Override
                        public void notFound() {
                            Toast.makeText(EventActivity.this, R.string.event_does_not_exists_anymore, Toast.LENGTH_SHORT).show();
                            EventActivity.this.onEventInaccessible();
                        }
                    })
                    .onError(new RetryOnErrorCallback(EventActivity.this, new RetryOnErrorCallback.OnRetryCallback() {
                        @Override
                        public void onRetry() {
                            try {
                                EventActivity.this.loadEvent();
                            } catch (CannotSaveModelException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }).setCancelable(false))
                    .onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {
                            loader.setVisibility(View.GONE);
                        }
                    })
                    .perform();
        }
        else{
            event = (Event) event.requireLocalId();
            Log.i(TAG, "Using cached event: " + event);
            onEventLoaded();
        }
    }

    private void initListeners() {

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : this is a workaround to enable scroll behind Fab when it's collapsed
                int visibility = btnAction.isExpanded() ? View.VISIBLE : View.GONE;
                btnActionTag.setVisibility(visibility);
                btnActionCamera.setVisibility(visibility);
                btnActionInvite.setVisibility(visibility);
                Log.d(TAG, "Main button action clicked!");
            }
        });

        btnActionTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(EventActivity.this, getEvent(), IntentsUtils.ACTION_TAGS);
            }
        });
        btnActionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(EventActivity.this, getEvent(), IntentsUtils.ACTION_CAMERA);
            }
        });
        btnActionInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(EventActivity.this, getEvent(), IntentsUtils.ACTION_PEOPLE);
            }
        });

    }

    /**
     * Method called when event has finished loading.
     * @warning Method must be called only ONCE.
     */
    private void onEventLoaded() {
        if (!isEventLoaded){
            isEventLoaded = true;

            event.addPropertyChangeListener(Event.PROPERTY_POINTS, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (!EventActivity.this.event.isAccessible()){
                        EventActivity.this.onEventInaccessible();
                    }
                    else{
                        if (fragmentInformation!= null && fragmentInformation.getView() != null) fragmentInformation.updatePointsView(EventActivity.this.event.getPoints());
                    }
                }

            });
            event.addPropertyChangeListener(Event.PROPERTY_PICTURE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    updateEventBackground();
                }
            });

            initFragments();
            parseIntentParameters();
        }

        updateView();
    }

    private void onEventInaccessible() {
        if (EventStatusManager.isCurrentEvent(eventId)){
            EventStatusManager.clearCurrentEvent();
        }
        EventActivity.this.exit();
    }

    private void parseIntentParameters() {
        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            parseActionParameter(extras.getInt(IntentsUtils.KEY_ACTION, -1));
        }
    }

    public void parseActionParameter(final int action){
        EventBaseFragment fragmentOriginPost;
        switch (action) {
            case IntentsUtils.ACTION_COMING:
                fragmentOriginPost = fragmentInformation;
                break;
            case IntentsUtils.ACTION_CAMERA:
                fragmentOriginPost = fragmentPictures;
                break;
            case IntentsUtils.ACTION_TAGS:
                fragmentOriginPost = fragmentTags;
                break;
            case IntentsUtils.ACTION_PEOPLE:
                fragmentOriginPost = fragmentPeople;
                break;
            default:
                Log.e(TAG,"No action is defined in method parseActionParameter");
                return;
        }
        //Check that fragment view has been created.
        if(fragmentOriginPost.getView()!=null) {
            actionPost(action);
        } else {
            fragmentOriginPost.setCreateViewCallback(new EventBaseFragment.OnCreateViewCallback() {
                @Override
                public void onCreateView() {
                    actionPost(action);
                }
            });
        }
    }

    private void actionPost(int action) {
        switch (action) {
            case IntentsUtils.ACTION_CAMERA:
                openAddPictureActivity();
                break;
            case IntentsUtils.ACTION_TAGS:
                openAddTagsActivity();
                break;
            case IntentsUtils.ACTION_PEOPLE:
                openAddPeopleActivity();
                break;
            case IntentsUtils.ACTION_COMING:
                fragmentInformation.turnComingOn();
                break;
        }
    }

    private void openAddTagsActivity() {
        if (!LocationManager.hasFineLocation()) {
            Toast.makeText(this, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }
        IntentsUtils.addTags(EventActivity.this, event);
    }

    private void openAddPictureActivity() {
        IntentsUtils.addPictureFromFragment(this, fragmentPictures);
    }

    private void openAddPeopleActivity() {
        IntentsUtils.inviteFriendToEvent(this, event);
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        mFragmentGroup = FragmentGroup.createGroup(this);
        fragmentInformation = (EventInformationFragment) mFragmentGroup.add((EventBaseFragment) Fragment.instantiate(this, EventInformationFragment.class.getName()));
        fragmentPictures = (EventPicturesFragment) mFragmentGroup.add((EventBaseFragment) Fragment.instantiate(this, EventPicturesFragment.class.getName()));
        fragmentTags = (EventTagsFragment) mFragmentGroup.add((EventBaseFragment) Fragment.instantiate(this, EventTagsFragment.class.getName()));
        fragmentPeople = (EventPeopleFragment) mFragmentGroup.add((EventBaseFragment) Fragment.instantiate(this, EventPeopleFragment.class.getName()));
        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        mFragmentAdapter = new EventPagerAdapter(this, getSupportFragmentManager(), mFragmentGroup.getFragments());

        mMaterialViewPager = (MaterialViewPager) findViewById(R.id.event_viewpager);
        mMaterialViewPager.getViewPager().setAdapter(mFragmentAdapter);
        //After set an adapter to the ViewPager
        mMaterialViewPager.getPagerTitleStrip().setViewPager(mMaterialViewPager.getViewPager());
        mMaterialViewPager.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), 0);

        this.updateEventBackground();
        int numberOfFragments = mFragmentAdapter.getCount();
        mMaterialViewPager.getViewPager().setOffscreenPageLimit(numberOfFragments);
        mMaterialViewPager.getViewPager().addOnPageChangeListener(new MyOnPageChangeListener(INITIAL_FRAGMENT_PAGE));

        /*
        Change header image and color
        mMaterialViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                Drawable drawable = null;
                try {
                    drawable = getResources().getDrawable(event.getCategory().getBigIcon());
                } catch (UnknownCategoryException e) {
                    drawable = getResources().getDrawable(R.drawable.image_else);
                }
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorAccent, drawable);
                    case 1:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorAccent, drawable);
                    case 2:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorAccent, drawable);
                    case 3:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorAccent, drawable);
                }

                //execute others actions if needed (ex : modify your header logo)

                return null;
            }
        });*/

        //initToolbar(false);
        Toolbar toolbar = mMaterialViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public void updateEventBackground() {
        if (event.hasPicture()){
            mMaterialViewPager.setImageUrl(event.getBackgroundUrl(), 0);
        }
        else{
            mMaterialViewPager.setImageDrawable(event.getBackgroundImage(this), 0);
        }
    }

    public void setCurrentPageSelected(int pageNumber) {
        mMaterialViewPager.getViewPager().setCurrentItem(pageNumber);
    }

    /**
     *
     */
    private void updateView() {
        pageTitle.setText(event.getName());
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
    /*public void requestForCameraPermission() {
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
    }*/

    public Event getEvent() {
        return event;
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
            mFragmentAdapter.getItem(mMaterialViewPager.getViewPager().getCurrentItem()).setMenuVisibility(true);
        }
    }

    public void setEvent(Event event) {
        this.event = event;
    }


    // =============================================================================================
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private int lastPosition;

        public MyOnPageChangeListener(int initialFragmentPage) {
            this.lastPosition = initialFragmentPage;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //Log.d(TAG, "onPageScrolled: " + position);
        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "Page " + position + " is now selected.");
            ((OnTabSelectedListener)mFragmentAdapter.getItem(position)).onTabSelected();
            ((OnTabSelectedListener)mFragmentAdapter.getItem(lastPosition)).onTabUnselected();
            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.d(TAG, "onPageScrollStateChanged: " + state);
        }
    }

}