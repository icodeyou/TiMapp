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
import android.view.View;
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
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.fragments.PlacePicturesFragment;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.EachSecondTimerTask;
import com.timappweb.timapp.utils.TimeTaskCallback;
import com.timappweb.timapp.utils.Util;

import java.io.File;
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
    private View        plusButtonView;
    private ListView    tagsListView;
    private ListView    placeListView;

    //Camera
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private int loadedFragments; // Number of fragments
    private PlacePicturesFragment fragmentPictures;
    private PlaceTagsFragment fragmentTags;
    private PlacePeopleFragment fragmentPosts;
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

    //Listeners
    private View.OnClickListener tagListener;
    private View.OnClickListener pictureListener;
    private View.OnClickListener peopleListener;
    private View progressBottom;


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
        iAmComingTv = (TextView) findViewById(R.id.text_coming_button);
        onMyWayButton = findViewById(R.id.button_on_my_way);
        progressBottom = findViewById(R.id.progressview_bottom_place);
        onMyWayTv = (TextView) findViewById(R.id.text_onmyway_button);
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

    private void initPlaceAdapters() {
        //PlacesAdapter
        placesAdapter = new PlacesAdapter(this, false);
        placeListView.setAdapter(placesAdapter);
        placeListView.setEnabled(false);
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
            this.uploadPicture(photoUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //private methods
    //////////////////////////////////////////////////////////////////////////////

    private void uploadPicture(final Uri fileUri) {
        fragmentPictures.setUploadVisibility(true);

        // create upload service client
        File file = new File(fileUri.getPath());

        if (!file.exists()){
            Log.d(TAG, "Photo does not exists: " + file.getAbsolutePath());
            return;
        }
        MediaType fileMimeType = MediaType.parse(Util.getMimeType(file.getAbsolutePath()));
        Log.d(TAG, "Photo '"+ file.getAbsolutePath() + "' has size: " + file.length() + " and type: " + fileMimeType);

        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        /*
        RequestBody requestFile = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("photo", file.getName())
                .addFormDataPart("photo", file.getName(),
                        RequestBody.create(fileMimeType, file))
                .build();*/
        //RequestBody requestFile = RequestBody.create(fileMimeType, file);

        // create RequestBody instance from file
       // RequestBody requestFile =
       //         RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        //MultipartBody.Part body =
        //        MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

        //Picture picture = new Picture();
        //picture.photo = file.getName();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("photo", file.getName())
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
            }

        });
        apiCalls.add(call);
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
        //PlaceActivity
        setSquareTouchListener(iAmComingButton, iAmComingTv);
        //Fragment Pictures
        //setSimpleTouchListener(fragmentPictures.getSmallTagsButton(),R.drawable.border_radius_top_left_selected);
        setSquareTouchListener(fragmentPictures.getMainButton(), fragmentPictures.getTvMainButton());
        //Fragment Tags
        //setSimpleTouchListener(fragmentTags.getSmallPicButton(),R.drawable.border_radius_top_right_selected);
        //setSimpleTouchListener(fragmentTags.getSmallPeopleButton(),R.drawable.border_radius_top_left_selected);
        setSquareTouchListener(fragmentTags.getMainButton(), fragmentTags.getTvMainButton());
        //Fragment Posts
        setSquareTouchListener(fragmentPosts.getMainButton(), fragmentPosts.getTvMainButton());
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        fragmentPictures = (PlacePicturesFragment) Fragment.instantiate(this, PlacePicturesFragment.class.getName());
        fragmentTags = (PlaceTagsFragment) Fragment.instantiate(this, PlaceTagsFragment.class.getName());
        fragmentPosts = (PlacePeopleFragment) Fragment.instantiate(this, PlacePeopleFragment.class.getName());

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
                Log.v(TAG,"position : " + position + ", positionOffsetPixels : " + positionOffsetPixels);

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
        apiCalls.add(call);
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
                plusButtonView.setVisibility(View.VISIBLE);
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

                if( progressBottom.getVisibility()!= View.VISIBLE) {
                    //if user hasn't stated he was coming
                    if(CacheData.isAllowedToAddUserStatus(place.id, UserPlaceStatus.COMING)) {
                        iAmComingButton.setVisibility(View.VISIBLE);
                        onMyWayButton.setVisibility(View.GONE);
                    }
                    else {
                        iAmComingButton.setVisibility(View.GONE);
                        onMyWayButton.setVisibility(View.VISIBLE);
                    }
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