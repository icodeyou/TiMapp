package com.timappweb.timapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.PlaceStatus;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.fragments.PlaceBaseFragment;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlacePicturesFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.views.EventView;

import java.util.Vector;

import retrofit2.Call;
import retrofit2.Response;

public class EventActivity extends BaseActivity {
    private String TAG = "EventActivity";
    private MyPagerAdapter pagerAdapter;
    private Place event;
    private int placeId;
    private Activity currentActivity;

    //Views
    private View        iAmComingButton;
    private TextView    iAmComingTv;
    private View        onMyWayButton;
    private TextView    onMyWayTv;
    private View        progressView;
    private ListView    tagsListView;
    private EventView   eventView;
    private View        progressBottom;
    private View        parentLayout;

    //Camera
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

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


    //Override methods
    //////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        this.event = IntentsUtils.extractPlace(getIntent());
        placeId = IntentsUtils.extractPlaceId(getIntent());
        if (event == null && placeId <= 0){
            Log.e(TAG, "Trying to view an invalid event --> redirect to home");
            IntentsUtils.home(this);
            return;
        }


        setContentView(R.layout.activity_event);
        Color color = new Color();
        initToolbar(false, color);

        //Initialize
        parentLayout = findViewById(R.id.main_layout_place);
        iAmComingButton = findViewById(R.id.button_coming);
        iAmComingTv = (TextView) findViewById(R.id.text_coming_button);
        onMyWayButton = findViewById(R.id.button_on_my_way);
        progressBottom = findViewById(R.id.progressview_bottom_place);
        onMyWayTv = (TextView) findViewById(R.id.text_onmyway_button);
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        progressView = findViewById(R.id.progress_view);

        initFragments();

        if (event != null){
            placeId = event.id;
            this.notifyEventLoaded();
        }
        else{
            loadPlace(placeId);
        };

        initLocationListener();
        setClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(1).setEnabled(true);
        return super.onPrepareOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_share:
                setDefaultShareIntent();
                return true;
            case R.id.action_reload:
                reloadData();
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
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            fragmentPictures.uploadPicture(photoUri);
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


    private void setClickListeners() {
        iAmComingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iAmComingButton.setVisibility(View.GONE);
                progressBottom.setVisibility(View.VISIBLE);
                // TODO fine location
                if (!MyApplication.hasLastLocation()) {
                    Toast.makeText(currentActivity, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());

                Call<RestFeedback> call = RestClient.service().notifyPlaceComing(conditions.toMap());
                call.enqueue(new RestFeedbackCallback(currentActivity) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        Log.d(TAG, "Success register coming for user on event " + placeId);
                        PlaceStatusManager.add(placeId, UserPlaceStatus.COMING);

                        progressBottom.setVisibility(View.GONE);
                        updateBtnVisibility();
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register coming for user on event " + placeId);
                        Toast.makeText(EventActivity.this,
                                getString(R.string.cannot_add_coming_status), Toast.LENGTH_SHORT).show();
                        iAmComingButton.setVisibility(View.VISIBLE);
                        progressBottom.setVisibility(View.GONE);
                    }
                });

            }
        });

        tagListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()) {
                    Toast.makeText(currentActivity, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }

                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
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

                IntentsUtils.addPostStepTags(currentActivity, event);
            }
        };

        pictureListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //placeActivity.requestForCameraPermission();
                takePicture();
            }
        };

        peopleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPeople(currentActivity, event);
            }
        };
    }


    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        childFragments = new Vector();

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
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        });
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


    private void loadPlace(int placeId) {
        final EventActivity that = this;
        Call<Place> call = RestClient.service().viewPlace(placeId);
        call.enqueue(new RestCallback<Place>(this) {
            @Override
            public void onResponse(Response<Place> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    event = response.body();
                    notifyEventLoaded();
                } else {
                    Toast.makeText(that, R.string.place_removed, Toast.LENGTH_LONG).show();
                    IntentsUtils.home(that);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                Toast.makeText(that, R.string.place_removed, Toast.LENGTH_LONG).show();
                IntentsUtils.home(that);
            }
        });
        apiCalls.add(call);
    }

    private void notifyEventLoaded() {
        progressView.setVisibility(View.GONE);
        try {
            EventCategory eventCategory = MyApplication.getCategoryById(event.category_id);
            ImageView backgroundImage = (ImageView) findViewById(R.id.background_place);
            backgroundImage.setImageResource(eventCategory.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + event.category_id);
        }
        fragmentTags.setEvent(event);
        updateBtnVisibility();
    }


    /**
     * Show or hide add post or coming button according to user location
     */
    public void updateBtnVisibility(){
        if(event != null && MyApplication.hasLastLocation()) {

            for (PlaceBaseFragment fragment: childFragments){
                if (fragment.isVisible()){
                    fragment.updateBtnVisibility();
                }
            }

            //if we are in the event
            boolean isUserComing = PlaceStatus.hasStatus(placeId, UserPlaceStatus.COMING);
            boolean isAllowedToAddComing = !isUserComing && QuotaManager.instance().checkQuota(QuotaType.NOTIFY_COMING);
            Boolean isAllowedToCome = !event.isAround() && isAllowedToAddComing;
            iAmComingButton.setVisibility(progressView.getVisibility() != View.VISIBLE && isAllowedToCome ? View.VISIBLE : View.GONE);
            onMyWayButton.setVisibility(event != null && !event.isAround() && progressView.getVisibility() != View.VISIBLE
                    && isUserComing ? View.VISIBLE : View.GONE);
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
            Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
            startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
        }
    }

    public void takePicture() {
        // Start CameraActivity
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public Place getEvent() {
        return event;
    }

    public int getPlaceId() {
        return placeId;
    }

    public View.OnClickListener getTagListener() {
        return tagListener;
    }

    public View.OnClickListener getPictureListener() {
        return pictureListener;
    }

    public View.OnClickListener getPeopleListener() {
        return peopleListener;
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isUserAround() {
        return this.event != null && this.event.isAround();
    }
}