package com.timappweb.timapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventPagerAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
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

import java.util.List;

public class EventActivity extends BaseActivity implements LocationManager.LocationListener{

    private String          TAG                     = "EventActivity";

    private static final int PAGER_INFO             = 0;
    public static final int PAGER_PICTURE           = 1;
    private static final int PAGER_TAG              = 2;
    private static final int PAGER_PEOPLE           = 3;

    private static final int INITIAL_FRAGMENT_PAGE  = 0;
    private static final int PAGER_OFFSCREEN_PAGE_LIMIT = 2;

    public static final int LOADER_ID_CORE          = 0;
    public static final int LOADER_ID_PICTURE       = 1;
    public static final int LOADER_ID_INVITATIONS   = 2;
    public static final int LOADER_ID_USERS         = 3;
    public static final int LOADER_ID_TAGS          = 4;

    private static final int REQUEST_CAMERA         = 0;

    // ---------------------------------------------------------------------------------------------

    private Event                       event;
    private int                         eventId;

    private EventPicturesFragment       fragmentPictures;
    private EventTagsFragment           fragmentTags;
    private EventPeopleFragment         fragmentPeople;
    private EventInformationFragment    fragmentInformation;

    private boolean                     isEventLoaded               = false;
    private FragmentGroup               mFragmentGroup;
    private MaterialViewPager           mMaterialViewPager;
    private EventPagerAdapter           mFragmentAdapter;
    private TextView                    pageTitle;
    private ViewDataBinding             mBinding;
    private FloatingActionButton        btnActionCamera;
    private FloatingActionButton        btnActionTag;
    private FloatingActionButton        btnActionInvite;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.event = IntentsUtils.extractEvent(getIntent());
        eventId = IntentsUtils.extractPlaceId(getIntent());
        if (event == null && eventId <= 0){
            Log.e(TAG, "Trying to view an invalid event --> redirect to home");
            IntentsUtils.home(this);
            return;
        }
        else if (eventId <= 0){
            eventId = event.remote_id;
        }


       mBinding = DataBindingUtil.setContentView(this, R.layout.activity_event);

        pageTitle = (TextView) findViewById(R.id.title_event);
        btnActionCamera = (FloatingActionButton) findViewById(R.id.action_camera);
        btnActionTag = (FloatingActionButton) findViewById(R.id.action_tag);
        btnActionInvite = (FloatingActionButton) findViewById(R.id.action_invite);


        getSupportLoaderManager().initLoader(LOADER_ID_CORE, null, new EventLoader());

        if (event != null){
            onEventLoaded();

            if (!event.hasLocalId()) {
                event = event.deepSave();
            }
        }

        initListeners();
    }

    private void initListeners() {

        btnActionTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!event.isUserAround()){
                    Toast.makeText(EventActivity.this, R.string.user_message_should_be_around_event_to_post, Toast.LENGTH_LONG).show();
                    return;
                }
                IntentsUtils.postEvent(EventActivity.this, getEvent(), IntentsUtils.ACTION_TAGS);
            }
        });
        btnActionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!event.isUserAround()){
                    Toast.makeText(EventActivity.this, R.string.user_message_should_be_around_event_to_post, Toast.LENGTH_LONG).show();
                    return;
                }
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
           // eventView.setEvent(event);
            initFragments();
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
                //mMaterialViewPager.setCurrentItem(0);
                break;
            case IntentsUtils.ACTION_TAGS:
                //mMaterialViewPager.setCurrentItem(1);
                openAddTagsActivity();
                break;
            case IntentsUtils.ACTION_PEOPLE:
                //mMaterialViewPager.setCurrentItem(2);
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
    protected void onStop() {
        LocationManager.stop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case IntentsUtils.REQUEST_CAMERA:
                if(resultCode==RESULT_OK) {
                    setCurrentPageSelected(PAGER_PICTURE);
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
        fragmentInformation = (EventInformationFragment) mFragmentGroup.add(Fragment.instantiate(this, EventInformationFragment.class.getName()));
        fragmentPictures = (EventPicturesFragment) mFragmentGroup.add(Fragment.instantiate(this, EventPicturesFragment.class.getName()));
        fragmentTags = (EventTagsFragment) mFragmentGroup.add(Fragment.instantiate(this, EventTagsFragment.class.getName()));
        fragmentPeople = (EventPeopleFragment) mFragmentGroup.add(Fragment.instantiate(this, EventPeopleFragment.class.getName()));

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        mFragmentAdapter = new EventPagerAdapter(getSupportFragmentManager(), mFragmentGroup.getFragments());
        mMaterialViewPager = (MaterialViewPager) findViewById(R.id.event_viewpager);
        mMaterialViewPager.getViewPager().setAdapter(mFragmentAdapter);
        //After set an adapter to the ViewPager
        mMaterialViewPager.getPagerTitleStrip().setViewPager(mMaterialViewPager.getViewPager());
        Drawable drawable = null;
        try {
            drawable = ResourcesCompat.getDrawable(getResources(), event.getCategory().getBigIcon(), null);
        } catch (UnknownCategoryException e) {
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.image_else, null);
        }
        mMaterialViewPager.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), 0);
        mMaterialViewPager.setImageDrawable(drawable, 0);
        mMaterialViewPager.getViewPager().setOffscreenPageLimit(PAGER_OFFSCREEN_PAGE_LIMIT); // Does not seem to work.

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
            mFragmentAdapter.getItem(mMaterialViewPager.getViewPager().getCurrentItem()).setMenuVisibility(true);
        }
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