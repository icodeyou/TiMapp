package com.timappweb.timapp.views;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.location.LocationManager;

import me.grantland.widget.AutofitTextView;

public class EventView extends RelativeLayout implements LocationManager.LocationListener {
    private final static String TAG = "EventView";
    private static final int TIMELAPSE_HOT_ANIM = 2000;
    private static final int TIMELAPSE_BUTTONS_APPEAR_ANIM = 800;
    private static final int TIMELAPSE_BUTTONS_DISAPPEAR_ANIM = 300;
    private Context context;

    private Place event;
    private ValueAnimator animator;

    private AutofitTextView             tvName;
    private TextView                    tvTime;
    private HorizontalTagsRecyclerView  tagsView;
    private ImageView                   categoryIcon;
    private ImageView                   smallCategoryIcon;
    private ImageView                   backgroundImage;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomViewIfPadding;
    private View                        gradientTopView;
    private SpotView                    spotView;
    private View                        titleLayout;
    private View                        gradientBottomView;
    private LinearLayout                mainLayoutEvent;
    private View                        marginToolbarRight;
    private View                        marginToolbarLeft;
    private View                        separator;
    private View                        descriptionView;
    private TextView                    descriptionTv;
    private HorizontalTagsAdapter       htAdapter;
    private HorizontalTagsRecyclerView  htrv;
    //private SelectableFloatingButton    matchButton;
    private TextView                    matchText;
    private View                        whitePointsLayout;

    private int                         colorSpot;
    private int                         colorEvent;
    private boolean                     isTagsVisible;
    private boolean                     isBottomShadow;
    private boolean                     isTopShadow;
    private boolean                     isSpot;
    private boolean                     isDescription;
    private boolean showEventTitle;
    private boolean                     isPointsVisible;
    private View distanceLayout;
    private TextView distanceText;
    private ImageView icPoints;

    private boolean hotPoints = false;
    private EventButtonsView eventButtonsView;


    public EventView(Context context) {
        super(context);
        this.context = context;
        this.isSpot = false;
        this.isTagsVisible = false;
        this.isPointsVisible = true;
        this.isDescription = true;
        this.colorEvent = ContextCompat.getColor(context, R.color.background_half_black);
        this.showEventTitle = false;
        this.init();
    }

    public EventView(Context context, boolean isBelowToolbarView) {
        super(context);
        this.context = context;
        this.isSpot = false;
        this.isTagsVisible = false;
        this.isPointsVisible = true;
        this.isDescription = true;
        this.colorEvent = ContextCompat.getColor(context, R.color.background_half_black);
        this.showEventTitle = isBelowToolbarView;
        this.init();
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EventView, 0, 0);
        isPointsVisible = ta.getBoolean(R.styleable.EventView_points_visible, true);
        isTagsVisible = ta.getBoolean(R.styleable.EventView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.EventView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.EventView_top_shadow, false);
        colorSpot = ta.getColor(R.styleable.EventView_color_spot, -1);
        colorEvent = ta.getColor(R.styleable.EventView_color_event, -1);
        isSpot = ta.getBoolean(R.styleable.EventView_is_spot, true);
        isDescription = ta.getBoolean(R.styleable.EventView_is_description, false);
        showEventTitle = ta.getBoolean(R.styleable.EventView_show_title, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_event, this);

        mainLayoutEvent = (LinearLayout) findViewById(R.id.main_layout_event);
        marginToolbarRight = findViewById(R.id.margin_right_toolbar);
        marginToolbarLeft = findViewById(R.id.margin_left_toolbar);
        spotView = (SpotView) findViewById(R.id.spot_view);
        titleLayout = findViewById(R.id.text_relative_layout);
        whitePointsLayout = findViewById(R.id.white_points_layout);
        tvName = (AutofitTextView) findViewById(R.id.title_event);
        icPoints = (ImageView) findViewById(R.id.ic_hot);
        tvCountPoints = (SimpleTimerView) findViewById(R.id.white_points_text);
        tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        smallCategoryIcon = (ImageView) findViewById(R.id.image_small_category_place);
        backgroundImage = (ImageView) findViewById(R.id.background_image_event);
        gradientBottomView = findViewById(R.id.bottom_gradient_event);
        //gradientBottomViewIfPadding = findViewById(R.id.bottom_gradient_if_padding);
        separator = findViewById(R.id.separator);
        descriptionView = findViewById(R.id.description_event);
        descriptionTv = (TextView) findViewById(R.id.description_textview);
        //matchButton = (SelectableFloatingButton) findViewById(R.id.match_button);
        distanceLayout = findViewById(R.id.distance_layout);
        distanceText = (TextView) findViewById(R.id.distance_text);

        tagsView = (HorizontalTagsRecyclerView) findViewById(R.id.htrv_tags);
        eventButtonsView = (EventButtonsView) findViewById(R.id.event_buttons_view);

        //htAdapter = (HorizontalTagsAdapter) rvEventTags.getAdapter();

        if(isSpot) {
            gradientTopView = findViewById(R.id.topview_no_spot);
        } else {
            gradientTopView = findViewById(R.id.topview_with_spot);
        }

        whitePointsLayout.setVisibility(isPointsVisible ? VISIBLE : GONE);
        gradientTopView.setVisibility(isTopShadow ? VISIBLE : GONE);
        gradientTopView.setVisibility(isTopShadow ? VISIBLE : GONE);
        gradientBottomView.setVisibility(isBottomShadow ? VISIBLE : GONE);

        setSpotVisible(isSpot);
    }

/*
    private void setComingOrHere() {
        if(tvCountPoints.getPoints()==0) {
            return;
        }
        boolean pointsAnimIsCurrent = animator!=null && animator.isRunning();

        if (!isMatchButtonSelected && !pointsAnimIsCurrent) {
            isMatchButtonSelected = true;
            //TODO : Add points on server
        }
        else if(isMatchButtonSelected && !pointsAnimIsCurrent){
            isMatchButtonSelected = false;
            //TODO : Remove points on server
        }
        updatePointsView(isMatchButtonSelected);
    }
*/

    public void updatePointsView(boolean increase) {
        if(increase && !hotPoints) {
            hotPoints = true;
            icPoints.setImageResource(R.drawable.ic_hot);
            tvCountPoints.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        } else if(!increase && hotPoints){
            hotPoints = false;
            icPoints.setImageResource(R.drawable.ic_hot_white);
            tvCountPoints.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            return;
        }

        animator = new ValueAnimator();
        tvCountPoints.cancelTimer();
        int initialPoints = tvCountPoints.getPoints();
        final int finalPoints;
        if(increase) {
            finalPoints = initialPoints+300;
        } else {
            if(initialPoints>300) {
                finalPoints = initialPoints-300;
            } else {
                finalPoints = 0;
            }
        }

        Log.d(TAG, "Initial points : " + initialPoints);
        Log.d(TAG, "Final points : " + finalPoints);
        animator.setObjectValues(initialPoints, finalPoints);
        animator.setDuration(TIMELAPSE_HOT_ANIM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvCountPoints.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.start();
        if(finalPoints==0) {
            Log.d(TAG, "Set timer text to Over");
            tvCountPoints.setText(context.getString(R.string.counter_over));
        } else {
            Log.d(TAG, "Initializing timer to +" + finalPoints);
            tvCountPoints.initTimer(finalPoints*1000 + TIMELAPSE_HOT_ANIM);
        }
    }

    public HorizontalTagsRecyclerView getRvEventTags() {
        return htrv;
    }



    private void setSpotVisible(boolean isSpot) {
        if(isSpot) {
            separator.setVisibility(VISIBLE);
            spotView.setVisibility(VISIBLE);
        } else {
            spotView.setVisibility(GONE);
            separator.setVisibility(GONE);
        }
    }



    public Place getEvent() {
        return event;
    }

    public void setEvent(final Place event) {

        this.event = event;
        this.eventButtonsView.setEvent(event);

        if (event == null){
            Log.e(TAG, "Trying to display a null event");
            return;
        }

        boolean isUserComing = PlaceStatusManager.hasStatus(event.remote_id, UserPlaceStatusEnum.COMING);
        boolean isHereStatus = PlaceStatusManager.hasStatus(event.remote_id, UserPlaceStatusEnum.HERE);
        boolean isHotView = isUserComing || isHereStatus ;

        //Date
        tvTime.setText(event.getTime());

        //Title
        tvName.setText(event.name);

        if(event.hasDescription()) {
            descriptionTv.setText(event.description);
        } else {
            descriptionTv.setVisibility(GONE);
        }

        //EventCategory
        EventCategory eventCategory = null;
        try {
            Log.d(TAG, "Setting event Background");
            eventCategory = MyApplication.getCategoryById(event.category_id);
            smallCategoryIcon.setImageResource(eventCategory.getIconWhiteResId());
            backgroundImage.setImageResource(eventCategory.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no eventCategory found for id : " + event.category_id);
        }

        if(colorEvent != -1) {
            Log.d(TAG,"Setting custom color");
            backgroundImage.setImageResource(0);
            mainLayoutEvent.setBackgroundColor(colorEvent);
            if (colorSpot != -1) {
                //spotView.setColor(colorSpot);
            }
        }

        descriptionView.setVisibility(isDescription && event.hasDescription() ? VISIBLE : GONE);


        if (isTagsVisible && event.hasTags()) {
            tagsView.getAdapter().setData(event.tags);
            tagsView.setVisibility(VISIBLE);
        }
        else{
            tagsView.setVisibility(GONE);
        }

        // Spot view
        if (event.spot != null && isSpot){
            spotView.setSpot(event.spot);
            this.setSpotVisible(true);
        }
        else{
            this.setSpotVisible(false);
        }

        //Counter
        int initialTime = event.getPoints();
        tvCountPoints.initTimer(initialTime * 1000);

        updatePointsView(isHotView);
        setDistance();

        LocationManager.addOnLocationChangedListener(this);
    }

    public void setDistance(){
        distanceLayout.setVisibility(VISIBLE);

        if (LocationManager.hasLastLocation()){
            event.updateDistanceFromUser();
            distanceText.setText(DistanceHelper.prettyPrint(event.getDistanceFromUser()));
        }
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        setDistance();
    }
}
