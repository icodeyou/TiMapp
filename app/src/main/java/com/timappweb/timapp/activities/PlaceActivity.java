package com.timappweb.timapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.timappweb.timapp.config.ServerConfiguration;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.fragments.PlacePicturesFragment;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.PlaceView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceActivity extends BaseActivity {
    private String TAG = "PlaceActivity";
    private MyPagerAdapter pagerAdapter;
    private Place place;
    private int placeId;
    private Activity currentActivity;

    //Views
    private View        iAmComingButton;
    private TextView    iAmComingTv;
    private View        onMyWayButton;
    private TextView    onMyWayTv;
    private View        progressView;
    private ListView    tagsListView;
    private PlaceView placeView;
    private View        progressBottom;
    private View        parentLayout;

    //Camera
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private int loadedFragments; // Number of fragments
    private PlacePicturesFragment fragmentPictures;
    private PlaceTagsFragment fragmentTags;
    private PlacePeopleFragment fragmentPeople;
    private ViewPager pager;

    private ShareActionProvider shareActionProvider;
    private int counter = 0;

    private LocationListener mLocationListener;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    //TODO : Update this variable
    private boolean isAllowedToAddPic = true;

    //Listeners
    private View.OnClickListener tagListener;
    private View.OnClickListener pictureListener;
    private View.OnClickListener peopleListener;


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
        parentLayout = findViewById(R.id.main_layout_place);
        iAmComingButton = findViewById(R.id.button_coming);
        iAmComingTv = (TextView) findViewById(R.id.text_coming_button);
        onMyWayButton = findViewById(R.id.button_on_my_way);
        progressBottom = findViewById(R.id.progressview_bottom_place);
        onMyWayTv = (TextView) findViewById(R.id.text_onmyway_button);
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeView = (PlaceView) findViewById(R.id.place_view);
        progressView = findViewById(R.id.progress_view);

        initFragments();

        if (place != null){
            placeId = place.id;
            this.notifyPlaceLoaded();
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
                reloadPlace();
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

    private void initPlaceView() {
        placeView.setPlace(place);
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
            this.uploadPicture(photoUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //private methods
    //////////////////////////////////////////////////////////////////////////////

    private void reloadPlace() {
        invalidateOptionsMenu();
        fragmentTags.setProgressView(true);
        fragmentPeople.setProgressView(true);
        fragmentPictures.setProgressView(true);
        fragmentPictures.loadPictures();
        fragmentTags.loadTags();
        fragmentPeople.load();
        updateButtonsVisibility();
    }

    private void uploadPicture(final Uri fileUri) {
        final Context context = this;
        fragmentPictures.setUploadVisibility(true);

        // create upload service client
        File file = new File(fileUri.getPath());

        try {
            // Compress the file
            Log.d(TAG, "BEFORE COMPRESSION: " +
                    "Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    ". Max size: " + Util.byteToKB(MyApplication.getApplicationRules().picture_max_size));

            ServerConfiguration.Rules rules = MyApplication.getApplicationRules();
            file = PictureUtility.resize(context, file, rules.picture_max_width, rules.picture_max_height);

            MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));

            Log.d(TAG, "AFTER COMPRESSION: Photo '"+ file.getAbsolutePath() + "'" +
                    " has size: " + Util.byteToKB(file.length()) +
                    " and type: " + fileMimeType);

            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("photo", file.getName(),
                            RequestBody.create(fileMimeType, file))
                    .build();

            // finally, execute the request
            Call<RestFeedback> call = RestClient.service().upload(this.placeId, body);
            call.enqueue(new Callback<RestFeedback>() {
                @Override
                public void onResponse(Response<RestFeedback> response) {
                    if (response.isSuccess()) {
                        RestFeedback feedback = response.body();

                        if (feedback.success) {
                            Log.v(TAG, "SUCCESS UPLOAD IMAGE");
                            fragmentPictures.setUploadVisibility(false);
                            // Get the bitmap in according to the width of the device
                            Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(fileUri.getPath(), 1000, 1000);
                            //fragmentPictures.getPicAdapter().addData(bitmap);
                            fragmentPictures.loadPictures();
                            fragmentPictures.getPicturesRv().smoothScrollToPosition(0);

                            Picture picture = new Picture();
                            picture.created = Util.getCurrentTimeSec();
                            picture.place_id = placeId;
                            CacheData.setLastPicture(picture);
                        } else {
                            Log.v(TAG, "FAILURE UPLOAD IMAGE: " + feedback.message);
                            fragmentPictures.setUploadVisibility(false);
                        }
                        Toast.makeText(currentActivity, feedback.message, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Upload error:" + t.getMessage());
                    fragmentPictures.setUploadVisibility(false);
                    Toast.makeText(context, "We cannot upload this image", Toast.LENGTH_LONG).show();
                }

            });
            apiCalls.add(call);
        } catch (IOException e) {
            Log.e(TAG, "Cannot resize picture: " + file.getAbsolutePath());
            e.printStackTrace();
            return ;
        }
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
                        Log.d(TAG, "Success register coming for user on place " + placeId);
                        com.timappweb.timapp.cache.CacheData.addUserStatus(placeId, UserPlaceStatus.COMING);
                        progressBottom.setVisibility(View.GONE);
                        updateButtonsVisibility();
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register coming for user on place " + placeId);
                        Toast.makeText(PlaceActivity.this,
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

                IntentsUtils.addPostStepTags(currentActivity, place);
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
                IntentsUtils.addPeople(currentActivity, place);
            }
        };
    }

    private void setTouchListeners() {
       /* //PlaceActivity
        iAmComingButton.setOnTouchListener(new ColorSquareOnTouchListener(this, iAmComingTv));
        //Fragment Pictures
        //setSimpleTouchListener(fragmentPictures.getSmallTagsButton(),R.drawable.border_radius_top_left_selected);
        fragmentPictures.getMainButton().setOnTouchListener(
                new ColorSquareOnTouchListener(this, fragmentPictures.getTvMainButton()));
        //Fragment Tags
        //setSimpleTouchListener(fragmentTags.getSmallPicButton(),R.drawable.border_radius_top_right_selected);
        //setSimpleTouchListener(fragmentTags.getSmallPeopleButton(),R.drawable.border_radius_top_left_selected);
        fragmentTags.getMainButton().setOnTouchListener(
                new ColorSquareOnTouchListener(this, fragmentTags.getTvMainButton()));
        //Fragment Posts
        fragmentPeople.getMainButton().setOnTouchListener(
                new ColorSquareOnTouchListener(this, fragmentPeople.getTvMainButton()));*/
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        fragmentPictures = (PlacePicturesFragment) Fragment.instantiate(this, PlacePicturesFragment.class.getName());
        fragmentTags = (PlaceTagsFragment) Fragment.instantiate(this, PlaceTagsFragment.class.getName());
        fragmentPeople = (PlacePeopleFragment) Fragment.instantiate(this, PlacePeopleFragment.class.getName());

        // Ajout des Fragments dans la liste
        fragments.add(fragmentPictures);
        fragments.add(fragmentTags);
        fragments.add(fragmentPeople);

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments);

        pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        pager.setOffscreenPageLimit(2);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);
        pager.setCurrentItem(1);
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
        apiCalls.add(call);
    }

    private void notifyPlaceLoaded() {
        progressView.setVisibility(View.GONE);
        try {
            Category category = MyApplication.getCategoryById(place.category_id);
            ImageView backgroundImage = (ImageView) findViewById(R.id.background_place);
            backgroundImage.setImageResource(category.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + place.category_id);
        }

        initPlaceView();
    }

    public void notifyFragmentsLoaded() {
        loadedFragments = loadedFragments + 1;
        if(loadedFragments>=3) {
            invalidateOptionsMenu();
            updateButtonsVisibility();
            //setTouchListeners();
        }
    }

    /**
     * Show or hide add post or coming button according to user location
     */
    public void updateButtonsVisibility(){
        if(place != null && MyApplication.hasLastLocation()) {
            if (fragmentPeople != null){
                fragmentPeople.updateBtnVisibility();
            }
            if (fragmentPictures != null){
                fragmentPictures.updateBtnVisibility();
            }
            if (fragmentTags != null){
                fragmentTags.updateBtnVisibility();
            }
            //if we are in the place
            Boolean isAllowedToCome = !place.isAround() && CacheData.isAllowedToAddUserStatus(place.id, UserPlaceStatus.COMING);
            iAmComingButton.setVisibility(progressView.getVisibility() != View.VISIBLE && isAllowedToCome ? View.VISIBLE : View.GONE);
            onMyWayButton.setVisibility(place != null && !place.isAround() && progressView.getVisibility() != View.VISIBLE && CacheData.isUserComing(place.id)  ? View.VISIBLE : View.GONE);
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

}