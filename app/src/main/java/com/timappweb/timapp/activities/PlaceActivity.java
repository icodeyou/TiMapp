package com.timappweb.timapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.desmond.squarecamera.ImageUtility;
import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.fragments.PlacePicturesFragment;
import com.timappweb.timapp.fragments.PlacePostsFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.EachSecondTimerTask;
import com.timappweb.timapp.utils.TimeTaskCallback;

import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Response;

public class PlaceActivity extends BaseActivity {
    private String TAG = "PlaceActivity";
    private MyPagerAdapter pagerAdapter;
    private Place place;
    private int placeId;
    private Activity currentActivity;

    //Views
    private View        iAmComingButton;
    private View        onMyWayButton;
    private View        progressView;
    private View        plusButtonView;
    private ListView    tagsListView;
    private ListView    placeListView;

    //Camera
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private int loadedFragments; // Number of fragments
    private PlacePicturesFragment fragmentPictures;
    private PlaceTagsFragment fragmentTags;
    private PlacePostsFragment fragmentPosts;
    private ViewPager pager;

    private ShareActionProvider shareActionProvider;
    private PlacesAdapter placesAdapter;
    private EachSecondTimerTask eachSecondTimerTask = null;
    private int counter = 0;

    private LocationListener mLocationListener;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    //TODO : Update this variable
    private boolean isAllowedToAddPic = true;


    //Override methods
    //////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        this.place = IntentsUtils.extractPlace(getIntent());
        placeId = IntentsUtils.extractPlaceId(getIntent());
        if (place == null && placeId <= 0){
            IntentsUtils.home(this);
            return;
        }
        loadedFragments = 0;

        setContentView(R.layout.activity_place);
        initToolbar(true);

        //Initialize
        iAmComingButton = findViewById(R.id.button_coming);
        onMyWayButton = findViewById(R.id.button_on_my_way);
        plusButtonView = findViewById(R.id.plus_button_view);
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeListView = (ListView) findViewById(R.id.place_lv);
        progressView = findViewById(R.id.progress_view);

        initFragments();
        initPlaceAdapters();
        initLocationListener();
        setClickListeners();



        if (place != null){
            placeId = place.id;
            this.notifyPlaceLoaded();
        }
        else{
            loadPlace(placeId);
        };
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
            case R.id.action_reload:
                IntentsUtils.reload(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void initPlaceAdapters() {
        //PlacesAdapter
        placesAdapter = new PlacesAdapter(this, false);
        placeListView.setAdapter(placesAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        eachSecondTimerTask.cancel();
    }
    @Override
    protected void onResume() {
        super.onResume();

        eachSecondTimerTask = EachSecondTimerTask.add(new TimeTaskCallback() {
            @Override
            public void update() {
                placesAdapter.notifyDataSetChanged();
                if(loadedFragments>=3) {
                    updateButtonsVisibility();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), 1000, 1000);
            fragmentPictures.setImage(bitmap);
            //TODO : Envoyer la photo au serveur à la place de l'action précédente
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //private methods
    //////////////////////////////////////////////////////////////////////////////

    private void setClickListeners() {
        final Activity that = this;

        iAmComingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()) {
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());

                Call<RestFeedback> call = RestClient.service().placeComing(conditions.toMap());
                call.enqueue(new RestFeedbackCallback(that) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        Log.d(TAG, "Success register coming for user on place " + placeId);
                        CacheData.addUserStatus(placeId, UserPlaceStatus.COMING);
                        updateButtonsVisibility();
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register coming for user on place " + placeId);
                    }
                });
            }
        });
    }

    private void setTouchListeners() {
        setMyTouchListener(onMyWayButton, R.color.colorAccentLight);
        setMyTouchListener(iAmComingButton, R.color.colorAccentLight);
        setMyTouchListener(fragmentPictures.getMainButton(),R.color.colorAccentLight);
        setMyTouchListener(fragmentTags.getMainButton(),R.color.colorAccentLight);
        setMyTouchListener(fragmentPosts.getMainButton(),R.color.colorAccentLight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        fragmentPictures = (PlacePicturesFragment) Fragment.instantiate(this, PlacePicturesFragment.class.getName());
        fragmentTags = (PlaceTagsFragment) Fragment.instantiate(this, PlaceTagsFragment.class.getName());
        fragmentPosts = (PlacePostsFragment) Fragment.instantiate(this, PlacePostsFragment.class.getName());

        // Ajout des Fragments dans la liste
        fragments.add(fragmentPictures);
        fragments.add(fragmentTags);
        fragments.add(fragmentPosts);

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments);

        pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        pager.setOffscreenPageLimit(2);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);
        pager.setCurrentItem(1);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG,"position : " + position);
                Log.i(TAG,"positionOffsetPixels : " + positionOffsetPixels);

                //If the user can't add pics nor tags
                if(!isAllowedToAddPic && !CacheData.isAllowedToAddPost()) {
                    if (position == 1) {
                        if(positionOffsetPixels < 700) {
                            plusButtonView.setVisibility(View.GONE);
                        } else plusButtonView.setVisibility(View.VISIBLE);
                    }
                }
                //If the user can add pics but not tags
                else if(isAllowedToAddPic && !CacheData.isAllowedToAddPost()) {
                    if (position == 0) {
                        if(positionOffsetPixels > 700) {
                            plusButtonView.setVisibility(View.GONE);
                        } else plusButtonView.setVisibility(View.VISIBLE);
                    }
                    if (position == 1) {
                        if(positionOffsetPixels < 700) {
                            plusButtonView.setVisibility(View.GONE);
                        } else plusButtonView.setVisibility(View.VISIBLE);
                    }
                }
                //If the user can add tags but not pics
                else if(!isAllowedToAddPic && CacheData.isAllowedToAddPost()) {
                    if (position == 0) {
                        if(positionOffsetPixels < 700) {
                            plusButtonView.setVisibility(View.GONE);
                        } else plusButtonView.setVisibility(View.VISIBLE);
                    }
                }
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
                    updateButtonsVisibility();
                }
            }
        };
        initLocationProvider(mLocationListener);
    }


    private void loadPlace(int placeId) {
        final PlaceActivity that = this;
        Call<Place> call = RestClient.service().viewPlace(placeId);
        call.enqueue(new RestCallback<Place>(this) {
            @Override
            public void onResponse(Response<Place> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    Place p = response.body();
                    place = p;
                    notifyPlaceLoaded();
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
    }

    private void notifyPlaceLoaded() {
        progressView.setVisibility(View.GONE);
        placesAdapter.add(place);
    }

    public void notifyFragmentsLoaded() {
        loadedFragments = loadedFragments + 1;
        if(loadedFragments>=3) {
            updateButtonsVisibility();
            setTouchListeners();
        }
    }

    /**
     * Show or hide add post or comming button according to user location
     */
    private void updateButtonsVisibility(){
        if(place != null && MyApplication.hasLastLocation()) {
            //if we are in the place
            if(place.isAround()) {
                iAmComingButton.setVisibility(View.GONE);
                fragmentPosts.setMainButtonVisibility(true);
                //if we can post a pic and a post
                if(isAllowedToAddPic && CacheData.isAllowedToAddPost()) {
                    setFragmentPicturesButtons(true);
                    setFragmentTagsButtons(true);
                }
                //if we can post tags but NOT a pic
                else if(!isAllowedToAddPic && CacheData.isAllowedToAddPost()) {
                        setFragmentPicturesButtons(false);
                        setFragmentTagsButtons(true);
                }
                //if we can post a pic but NOT tags
                else if(isAllowedToAddPic && !CacheData.isAllowedToAddPost()) {
                    setFragmentPicturesButtons(true);
                    setFragmentTagsButtons(false);
                }
                //if we can't post a pic NOR a tag
                else if(!isAllowedToAddPic && !CacheData.isAllowedToAddPost()) {
                    fragmentTags.setMainButtonVisibility(false);
                    fragmentTags.setSmallPicButtonVisibility(false);
                    fragmentTags.setSmallPeopleButtonVisibility(true);
                    fragmentPictures.setMainButtonVisibility(false);
                    fragmentPictures.setSmallTagsButtonVisibility(false);
                }
            }
            //if we are away from the place
            else {
                plusButtonView.setVisibility(View.GONE);
                fragmentTags.setMainButtonVisibility(false);
                fragmentTags.setSmallPicButtonVisibility(false);
                fragmentTags.setSmallPeopleButtonVisibility(false);
                fragmentPictures.setMainButtonVisibility(false);
                fragmentPictures.setSmallTagsButtonVisibility(false);
                fragmentPosts.setMainButtonVisibility(false);

                //if user hasn't stated he was coming
                if(CacheData.isAllowedToAddUserStatus(place.id, UserPlaceStatus.COMING)) {
                    iAmComingButton.setVisibility(View.VISIBLE);
                    onMyWayButton.setVisibility(View.GONE);
                }
                else {
                    iAmComingButton.setVisibility(View.VISIBLE);
                    onMyWayButton.setVisibility(View.GONE);
                }
            }

            pagerAdapter.getItem(pager.getCurrentItem()).setMenuVisibility(true);
        }
    }

    private void setFragmentTagsButtons(boolean isAllowedToAddPost) {
        fragmentTags.setMainButtonVisibility(isAllowedToAddPost);
        fragmentTags.setSmallPicButtonVisibility(!isAllowedToAddPost);
        fragmentTags.setSmallPeopleButtonVisibility(!isAllowedToAddPost);
    }

    private void setFragmentPicturesButtons(boolean isAllowedToAddPic) {
        fragmentPictures.setMainButtonVisibility(isAllowedToAddPic);
        fragmentPictures.setSmallTagsButtonVisibility(!isAllowedToAddPic);
    }


    private void setDefaultShareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_place_text));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    //Public methods
    //////////////////////////////////////////////////////////////////////////////

    public void setPlusButtonVisibility(boolean bool) {
        if(bool) {
            plusButtonView.setVisibility(View.VISIBLE);
        }
        else plusButtonView.setVisibility(View.GONE);
    }

    // Check for camera permission in MashMallow
    public void requestForCameraPermission() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show permission rationale
            } else {
                // Handle the result in Activity#onRequestPermissionResult(int, String[], int[])
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

    public Place getPlace() {
        return place;
    }

    public int getPlaceId() {
        return placeId;
    }

}