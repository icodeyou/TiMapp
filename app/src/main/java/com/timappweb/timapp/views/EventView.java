package com.timappweb.timapp.views;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
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

    private Event event;
    private ValueAnimator animator;

    private TextView tvName;
    private TextView                    tvTime;
    private HorizontalTagsRecyclerView  tagsView;
    private ImageView                   categoryIcon;
    private ImageView                   smallCategoryIcon;
    private ImageView                   backgroundImage;
    private View                        gradientBottomViewIfPadding;
    private View                        gradientTopView;
    private SpotView                    spotView;
    private View                        titleLayout;
    private View                        gradientBottomView;
    //private View                        separator;
    //private View                        descriptionView;
    private TextView                    descriptionTv;
    private HorizontalTagsAdapter       htAdapter;
    private HorizontalTagsRecyclerView  htrv;
    //private SelectableFloatingButton    matchButton;
    private TextView                    matchText;
    private View                        whitePointsLayout;

    private boolean showEventTitle;
    private boolean                     isPointsVisible;
    private View distanceLayout;
    private TextView distanceText;
    private ImageView icPoints;

    private boolean hotPoints = false;
    //private EventButtonsView eventButtonsView;
    private TextView tvCountComing;
    private TextView tvCountHere;


    public EventView(Context context) {
        super(context);
        this.context = context;
        this.isPointsVisible = true;
        this.showEventTitle = false;
        this.init();
    }

    public EventView(Context context, boolean isBelowToolbarView) {
        super(context);
        this.context = context;
        this.isPointsVisible = true;
        this.showEventTitle = isBelowToolbarView;
        this.init();
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EventView, 0, 0);
        isPointsVisible = ta.getBoolean(R.styleable.EventView_points_visible, true);
        showEventTitle = ta.getBoolean(R.styleable.EventView_show_title, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_event, this);

        //marginToolbarRight = findViewById(R.id.margin_right_toolbar);
        //marginToolbarLeft = findViewById(R.id.margin_left_toolbar);
        spotView = (SpotView) findViewById(R.id.spot_view);
        titleLayout = findViewById(R.id.event_title_container);
        tvName = (TextView) findViewById(R.id.title_event);
        //tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        smallCategoryIcon = (ImageView) findViewById(R.id.image_small_category_place);
        backgroundImage = (ImageView) findViewById(R.id.background_image_event);
        //gradientBottomView = findViewById(R.id.bottom_gradient_event);
        //gradientBottomViewIfPadding = findViewById(R.id.bottom_gradient_if_padding);
        //separator = findViewById(R.id.separator);
       //descriptionView = findViewById(R.id.description_event);
        //descriptionTv = (TextView) findViewById(R.id.description_textview);
        //matchButton = (SelectableFloatingButton) findViewById(R.id.match_button);
        distanceLayout = findViewById(R.id.distance_layout);
        distanceText = (TextView) findViewById(R.id.distance_text);

        //tagsView = (HorizontalTagsRecyclerView) findViewById(R.id.htrv_tags);
        //eventButtonsView = (EventButtonsView) findViewById(R.id.event_buttons_view);

        //htAdapter = (HorizontalTagsAdapter) rvEventTags.getAdapter();

        /*
        if(isSpot) {
            gradientTopView = findViewById(R.id.topview_no_spot);
        } else {
            gradientTopView = findViewById(R.id.topview_with_spot);
        }*/

        whitePointsLayout.setVisibility(isPointsVisible ? VISIBLE : GONE);

        //setSpotVisible(isSpot);
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
            //icPoints.setImageResource(R.drawable.ic_hot);
            //tvCountPoints.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        } else if(!increase && hotPoints){
            hotPoints = false;
            //icPoints.setImageResource(R.drawable.ic_hot_white);
            //tvCountPoints.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            return;
        }

        animator = new ValueAnimator();
        /*
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
        }*/
    }

    public HorizontalTagsRecyclerView getRvEventTags() {
        return htrv;
    }


/*
    private void setSpotVisible() {
        if(isSpot && event.hasSpot()) {
            spotView.setSpot(event.spot);
            //separator.setVisibility(VISIBLE);
            spotView.setVisibility(VISIBLE);
        } else {
            spotView.setVisibility(GONE);
            //separator.setVisibility(GONE);
        }
    }*/



    public Event getEvent() {
        return event;
    }

    public void setEvent(final Event event) {

        this.event = event;
        //if (this.eventButtonsView != null){
        //    this.eventButtonsView.setEvent(event);
        //}

        if (event == null){
            Log.e(TAG, "Trying to display a null event");
            return;
        }

        boolean isUserComing = PlaceStatusManager.hasStatus(event.remote_id, UserPlaceStatusEnum.COMING);
        boolean isHereStatus = PlaceStatusManager.hasStatus(event.remote_id, UserPlaceStatusEnum.HERE);
        boolean isHotView = isUserComing || isHereStatus ;

        //Date
        //tvTime.setText(event.getTime());

        //Title
        tvName.setText(event.name);
/*
        if(event.hasDescription()) {
            descriptionTv.setText(event.description);
        } else {
            descriptionTv.setVisibility(GONE);
        }*/

        //EventCategory

        EventCategory eventCategory = null;
        try {
            Log.d(TAG, "Setting event Background");
            eventCategory = MyApplication.getCategoryById(event.category_id);
            //smallCategoryIcon.setImageResource(eventCategory.getIconWhiteResId());
            backgroundImage.setImageResource(eventCategory.getBigImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no eventCategory found for id : " + event.category_id);
        }
        //descriptionView.setVisibility(isDescription && event.hasDescription() ? VISIBLE : GONE);

/*
        if (isTagsVisible && event.hasTags()) {
            tagsView.getAdapter().setData(event.tags);
            tagsView.setVisibility(VISIBLE);
        }
        else{
            tagsView.setVisibility(GONE);
        }*/

        //this.setSpotVisible();

        //Counter
        int initialTime = event.getPoints();

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
