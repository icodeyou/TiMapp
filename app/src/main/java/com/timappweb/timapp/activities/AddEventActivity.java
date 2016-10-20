package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventCategoriesAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.databinding.ActivityAddEventBinding;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.map.MapFactory;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallbackBinding;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.io.serializers.AddEventMapper;
import com.timappweb.timapp.rest.io.serializers.AddPictureMapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.PictureUtility;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.CategorySelectorView;
import com.timappweb.timapp.views.ConfirmDialog;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.MultipartBody;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import retrofit2.Call;
import retrofit2.Response;


public class AddEventActivity extends BaseActivity implements LocationManager.LocationListener, OnMapReadyCallback {

    private String                  TAG                                 = "AddEventActivity";
    private static final float      ZOOM_LEVEL_CENTER_MAP               = 14.0f;
    private static final int        NUMBER_OF_MAIN_CATEGORIES           = 4;
    private static final int        CATEGORIES_COLUMNS                  = 4;

    //----------------------------------------------------------------------------------------------

    private InputMethodManager          imm;
    private EditText                    eventNameET;
    private EventCategory               eventCategorySelected;
    private View                        progressView;
    private EditText                    descriptionET;
    private CategorySelectorView        categorySelector;

    // Data
    private MapView                     mapView = null;
    private GoogleMap                   gMap;
    private ActivityAddEventBinding     mBinding;
    private View                        mBtnAddSpot;
    private View                        mBtnAddPic;
    private View                        mSpotContainer;
    private ScrollView                  scrollView;
    private SimpleDraweeView            simpleDraweeView;
    private View                        icPicture;
    private View                        icPictureValidate;
    private View                        icSpot;
    private View                        icSpotValidate;
    private TextView                    icSpotText;
    private TextView                    icPictureText;

    private MenuItem                    postButton;

    //private AddressResultReceiver       mAddressResultReceiver;
    private HttpCallManager clientCall;
    private File        pictureSelected;
    private View.OnClickListener onSpotClickListener;
    private Location mFineLocation  = null;
    private View mWaitingForLocationLayout;
    private TextView mWaitingForLocationText;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_event);

        this.initToolbar(true);
        Util.setStatusBarColor(this, R.color.colorSecondaryDark);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        descriptionET = (EditText)  findViewById(R.id.description_edit_text);
        eventNameET = (EditText) findViewById(R.id.event_name);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.image_event);

        categorySelector = (CategorySelectorView) findViewById(R.id.category_selector);

        progressView = findViewById(R.id.progress_view);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(null);
        //mButtonAddPicture = findViewById(R.id.button_take_picture);
        mBtnAddSpot = findViewById(R.id.button_add_spot);
        mBtnAddPic = findViewById(R.id.button_add_picture);
        mSpotContainer = findViewById(R.id.spot_container);
        mWaitingForLocationLayout = findViewById(R.id.waiting_for_location_layout);
        mWaitingForLocationText = (TextView) findViewById(R.id.text_waiting_for_location);

        icPicture = findViewById(R.id.picture);
        icPictureValidate = findViewById(R.id.picture_validate);
        icPictureText= (TextView) findViewById(R.id.picture_text);
        icSpot = findViewById(R.id.spot);
        icSpotValidate = findViewById(R.id.spot_validate);
        icSpotText = (TextView) findViewById(R.id.spot_text);

        // @warning DO NOT MOVE
        mBinding.setEvent(new Event());
        extractSpot(savedInstanceState);
        // ------

        initContextMenu();
        initEts();
        initAdapterAndManager();
        setListeners();
        //initViewPager();

        if (!this.getResources().getBoolean(R.bool.eventcard_showProgressBarLocation)){
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }
    }

    private void initContextMenu() {
        registerForContextMenu(simpleDraweeView);
        registerForContextMenu(mBtnAddPic);
        registerForContextMenu(mSpotContainer);
        registerForContextMenu(mBtnAddSpot);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v == mBtnAddPic) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu_edit_picture, menu);
        }
        else if (v == mBtnAddSpot) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu_edit_spot, menu);
        }

        // @warning: we must wait that the menu has been created to call this function.
        // Otherwise we could have a null pointer on the menu when updating button visibility
        updateEventLocation();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_picture:
                IntentsUtils.attachPictureToEvent(this);
                return true;
            case R.id.action_remove_picture:
                simpleDraweeView.setVisibility(View.GONE);
                pictureSelected = null;
                icPicture.setVisibility(View.VISIBLE);
                icPictureValidate.setVisibility(View.GONE);
                icPictureText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                return true;
            case R.id.action_edit_spot:
                IntentsUtils.attachSpot(AddEventActivity.this, mBinding.getEvent().getSpot());
                return true;
            case R.id.action_remove_spot:
                mBinding.setEvent(mBinding.getEvent().setSpot(null));
                icSpot.setVisibility(View.VISIBLE);
                icSpotValidate.setVisibility(View.GONE);
                icSpotText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void initDebugView() {
        eventNameET.setText(getResources().getString(R.string.dev_name));
        descriptionET.setText(getResources().getString(R.string.dev_description));
    }

    @Override
    public void onBackPressed() {
        if(clientCall != null && clientCall.isDone()) {
            clientCall.cancel();
            clientCall = null;
            progressView.setVisibility(View.GONE);
        }
        else {
            showConfirmDialog();
        }
    }
    //----------------------------------------------------------------------------------------------
    //Private methods

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
        LocationManager.addOnLocationChangedListener(this);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() Stopping LocationManager");
        LocationManager.removeLocationListener(this);
        LocationManager.stop(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        postButton = menu.findItem(R.id.action_post);
        //setButtonValidation();

        if(BuildConfig.BUILD_TYPE == "debug") initDebugView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                if(!Event.isValidName(eventNameET.getText().toString().trim())) {
                    Log.d(TAG, "Name is not valid");
                    Toast.makeText(getBaseContext(), R.string.error_no_name, Toast.LENGTH_LONG).show();
                    return true;
                }
                if(eventCategorySelected == null) {
                    Log.d(TAG, "Category is not valid");
                    Toast.makeText(getBaseContext(), R.string.error_no_category, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (mFineLocation == null) {
                    if (LocationManager.hasLastLocation()) {
                        Toast.makeText(getBaseContext(), R.string.no_fine_location, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    Log.d(TAG, "Click on add event before having a user location");
                    Toast.makeText(getBaseContext(), R.string.no_fine_location, Toast.LENGTH_LONG).show();
                    return true;
                }
                setProgressView(true);
                Event event = mBinding.getEvent();
                event.setCategory(eventCategorySelected);
                event.setLocation(mFineLocation);
                submitEvent(event, pictureSelected);
                return true;
            case android.R.id.home:
                showConfirmDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showConfirmDialog() {
        ConfirmDialog.builder(this,
                null,
                getString(R.string.confim_message_add_event),
                getString(R.string.alert_dialog_continue_addevent),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == DialogInterface.BUTTON_POSITIVE) {
                            IntentsUtils.getBackToParent(AddEventActivity.this);
                        }
                    }
                }
        )
                .create()
                .show();;
    }

    private void initEts() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(filters);
        eventNameET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        //To remove words suggestions, add InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        InputFilter[] f = new InputFilter[1];
        f[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(f);
    }

    private void initAdapterAndManager() {
        //Main categories
        final EventCategoriesAdapter mainAdapter = new EventCategoriesAdapter(this,false);
        final EventCategoriesAdapter allAdapter = new EventCategoriesAdapter(this,true);

        mainAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(mainAdapter.getCategory(position));
            }
        });
        allAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(allAdapter.getCategory(position));
            }
        });

        categorySelector.setAdapters(mainAdapter, allAdapter);
    }

    private void setProgressView(boolean bool) {
        progressView.setVisibility(bool ? View.VISIBLE : View.INVISIBLE);
        postButton.setEnabled(!bool);
    }

    private void submitEvent(final Event event, File photo){
        Log.d(TAG, "Submit event " + event.toString());

        Call call;
        if (photo != null){
            MultipartBody body = new AddPictureMapper(photo)
                    .getBuilder()
                    .add(AddEventMapper.toJson(event), null)
                    .build();
            call = RestClient.service().addPlace(body);
        }
        else{
            call = RestClient.service().addPlace(AddEventMapper.toJson(event));
        }
        clientCall = RestClient.<JsonObject>buildCall(call);
        clientCall
                .onResponse(new AutoMergeCallback(event))
                .onResponse(new FormErrorsCallbackBinding(mBinding))
                .onResponse(new FormErrorsCallback(this, "Pictures"))
                .onResponse(new HttpCallback<JsonObject>() {
                    @Override
                    public void successful(JsonObject feedback) throws CannotSaveModelException {
                        Log.d(TAG, "Event has been successfully added");
                        event.setAuthor(MyApplication.getCurrentUser());
                        event.deepSave();
                        long syncId = feedback.get("places_users").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                        QuotaManager.instance().add(QuotaType.ADD_EVENT);
                        EventStatusManager.addLocally(syncId, event, UserEventStatusEnum.HERE);
                        IntentsUtils.viewEventFromId(AddEventActivity.this, event.remote_id);
                    }

                    @Override
                    public void notSuccessful() {
                        if (this.response.code() != HttpURLConnection.HTTP_BAD_REQUEST){
                            Toast.makeText(AddEventActivity.this, R.string.error_default, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        if (photo != null){
            clientCall.onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser(), QuotaType.ADD_PICTURE, false));
        }
        clientCall
                .onError(new NetworkErrorCallback(this))
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        if (error != null || !response.isSuccessful()){
                            setProgressView(false);
                        }
                    }

                })
                .perform();
    }

    //----------------------------------------------------------------------------------------------
    //Public methods

    public int getNumberOfMainCategories() {
        return NUMBER_OF_MAIN_CATEGORIES;
    }

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(EventCategory eventCategory) {
        eventCategorySelected = eventCategory;
        categorySelector.selectCategoryUI(eventCategory.getName(),eventCategory.getIconDrawable(AddEventActivity.this));
        //setButtonValidation();
    }

    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    private void setListeners() {
        //If click on editText when Not Focused
        View.OnFocusChangeListener onEtFocus = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                categorySelector.lowerView();
            }
        };
        //If click on editText when Focused
        View.OnClickListener onEtClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categorySelector.lowerView();
            }
        };
        eventNameET.setOnFocusChangeListener(onEtFocus);
        eventNameET.setOnClickListener(onEtClick);
        descriptionET.setOnFocusChangeListener(onEtFocus);
        descriptionET.setOnClickListener(onEtClick);

        categorySelector.setOnCrossClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        View.OnClickListener onPicClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pictureSelected==null) {
                    IntentsUtils.attachPictureToEvent(AddEventActivity.this);
                }
                else {
                    openContextMenu(mBtnAddPic);
                }
            }
        };

        onSpotClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.getEvent().getSpot() == null) {
                    IntentsUtils.attachSpot(AddEventActivity.this);
                } else {
                    openContextMenu(mBtnAddSpot);
                }
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                closeContextMenu();
                return true;
            }
        };

        mBtnAddPic.setOnClickListener(onPicClickListener);
        simpleDraweeView.setOnClickListener(onPicClickListener);
        mBtnAddSpot.setOnClickListener(onSpotClickListener);
        mSpotContainer.setOnClickListener(onSpotClickListener);

        mBtnAddPic.setOnLongClickListener(onLongClickListener);
        mBtnAddSpot.setOnLongClickListener(onLongClickListener);

        /*eventNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setButtonValidation();
            }
        });*/
    }

    public Event getEvent(){
        return mBinding.getEvent();
    }

    public Location getFineLocation(){
        return mFineLocation;
    }

    private void extractSpot(Bundle bundle){
        if(bundle!=null) {
            Spot spot = SerializeHelper.unpackModel(bundle.getString(IntentsUtils.KEY_SPOT), Spot.class);
            mBinding.getEvent().setSpot(spot);
            mSpotContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // TODO [jack][clean] WTF !?
        if(eventNameET.hasFocus()) {
            eventNameET.clearFocus();
            descriptionET.clearFocus();
        }
        else if(descriptionET.hasFocus()) {
            descriptionET.clearFocus();
            eventNameET.clearFocus();
        }
        imm.hideSoftInputFromWindow(eventNameET.getWindowToken(), 0);   //Hide keyboard
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        this.loadMapIfNeeded();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        LocationManager.removeLocationListener(this);
        super.onPause();
    }

    private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                //mapView.onCreate(null); // ???
                mapView.getMapAsync(this);
            }
            Location location = LocationManager.getLastLocation();
            if (location != null){
                updateMapCenter(location);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMapCenter(Location location){
        if (gMap != null){
            LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
            gMap.clear();
            gMap.addMarker(new MarkerOptions().position(coordinates));
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, ZOOM_LEVEL_CENTER_MAP));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        eventNameET.clearFocus();
        switch (requestCode){
            case IntentsUtils.REQUEST_PICK_SPOT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Spot");
                    updateUiAfterPickSpot();
                    extractSpot(data.getExtras());
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);

        final Activity activity = this;
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(activity, R.string.error_camera, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                Log.d(TAG, "Result request camera");
                if (resultCode != Activity.RESULT_OK){
                    Log.e(TAG, "Activity result for requesting camera returned a non success code: " + resultCode);
                    Toast.makeText(activity, R.string.error_camera, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    //Compress image and display it in Simple Drawee View
                    File compressedFile = AddPictureMapper.compress(imageFile);
                    simpleDraweeView.setImageURI(Uri.fromFile(compressedFile));

                    Bitmap bmp = BitmapFactory.decodeFile(imageFile.toString());
                    try {
                        bmp = PictureUtility.rotateBitmapIfNeeded(bmp, imageFile);
                    } catch (IOException e) {
                        Log.e(TAG, "Couldn't rotate image");
                        e.printStackTrace();
                    }
                    int imageHeight = bmp.getHeight();
                    int imageWidth = bmp.getWidth();
                    float ratio = (float) imageWidth/imageHeight;

                    simpleDraweeView.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = simpleDraweeView.getLayoutParams();
                    params.height = (int) (simpleDraweeView.getWidth()/ratio);
                    simpleDraweeView.setLayoutParams(params);

                    updateUiAfterPickTure();

                    pictureSelected = compressedFile;

                }
                catch (AddPictureMapper.CannotUploadPictureException e) {
                    Toast.makeText(AddEventActivity.this, getString(e.getResId()), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(activity);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    private void updateUiAfterPickSpot() {
        scrollView.smoothScrollTo(0,0);
        icSpot.setVisibility(View.GONE);
        icSpotValidate.setVisibility(View.VISIBLE);
        icSpotText.setTextColor(ContextCompat.getColor(this, R.color.selection_button_add_event));
        icSpot.setEnabled(false);
    }

    private void updateUiAfterPickTure() {
        //TODO : Scroll To Bottom doesn't work first time
        scrollView.fullScroll(View.FOCUS_DOWN);

        icPicture.setVisibility(View.GONE);
        icPictureValidate.setVisibility(View.VISIBLE);
        icPictureText.setTextColor(ContextCompat.getColor(this, R.color.selection_button_add_event));
        icPicture.setEnabled(false);
    }



    /**
     * Update event location.
     * If there is an up to date user location and if it's precise enough, we can stop requesting user location
     * Otherwise it will go on util finding a fine enough location
     */
    private void updateEventLocation(){
        if (!LocationManager.hasLastLocation()){
            Log.i(TAG, "We don't have user location yet.");
            return;
        }

        Location newLocation = LocationManager.getLastLocation();
        int accuracyRequired = ConfigurationProvider.rules().gps_min_accuracy_add_place;
        if (LocationManager.hasUpToDateLastLocation() && LocationManager.hasFineLocation(accuracyRequired)){
            Log.i(TAG, "A fine user location has been found: " + newLocation + ". Stopping location updates.");
            mFineLocation = newLocation;
            LocationManager.stop(this);
            onFineLocationFound();
        }
        else {
            Log.d(TAG, "The new location is not precise enough to add an event: " + newLocation.getAccuracy() + " > " + accuracyRequired);
            updateMapCenter(newLocation);
            if (BuildConfig.DEBUG){
                mWaitingForLocationText.setText(getString(R.string.waiting_for_location) + " (Gps accuracy: " + newLocation.getAccuracy() + ")");
            }
        }
    }

    private void onFineLocationFound() {
        //setButtonValidation();
        mWaitingForLocationLayout.setVisibility(View.GONE);
    }
    // =============================================================================================



    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        Log.d(TAG, "User location changed: " + newLocation);

        synchronized (this){
            if (mFineLocation == null){
                updateEventLocation();
                updateMapCenter(newLocation);
            }
        }
        //requestReverseGeocoding(newLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is now ready!");
        gMap = googleMap;
        MapFactory.initMap(gMap, false);

        if (LocationManager.hasLastLocation()){
            updateMapCenter(LocationManager.getLastLocation());
        }
    }

    // =============================================================================================

    /*
    private void requestReverseGeocoding(Location location){
        if (mAddressResultReceiver == null){
            mAddressResultReceiver = new AddressResultReceiver();
        }
        ReverseGeocodingHelper.request(this, location, mAddressResultReceiver);
    }

    public View getProgressBar() {
        return progressView;
    }

    // =============================================================================================

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            if (resultCode == Constants.SUCCESS_RESULT) {
                mBinding.setAddress(resultData.getString(Constants.RESULT_DATA_KEY));
            }
        }
    }*/
}
