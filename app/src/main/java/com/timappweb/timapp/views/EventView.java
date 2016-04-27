package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import me.grantland.widget.AutofitTextView;

public class EventView extends RelativeLayout{
    private final static String TAG = "EventView";

    private Place event;

    private View                        tagsView;
    private AutofitTextView             tvName;
    private TextView                    tvTime;
    private HorizontalTagsRecyclerView  rvEventTags;
    private ImageView                   categoryIcon;
    private ImageView                   smallCategoryIcon;
    private View                        pointsLayout;
    private ImageView                   backgroundImage;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomViewIfPadding;
    private View                        gradientTopView;
    private RelativeLayout              mainBox;
    private SpotView                    spotView;
    private View                        titleLayout;
    private View                        gradientBottomView;
    private LinearLayout                mainLayoutEvent;
    private View                        marginToolbarRight;
    private View                        marginToolbarLeft;
    private View                        separator;
    private View                        descriptionView;

    private int                         colorSpot;
    private int                         colorEvent;
    private boolean                     isTagsVisible;
    private boolean                     isBottomShadow;
    private boolean                     isTopShadow;
    private boolean                     isPadding;
    private boolean                     isSpot;
    private boolean                     isDescription;
    private boolean                     toolbarMode;
    private boolean                     belowToolbar;


    public EventView(Context context) {
        super(context);
        this.init();
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EventView, 0, 0);
        isTagsVisible = ta.getBoolean(R.styleable.EventView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.EventView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.EventView_top_shadow, false);
        colorSpot = ta.getColor(R.styleable.EventView_color_spot, -1);
        colorEvent = ta.getColor(R.styleable.EventView_color_event, -1);
        isPadding = ta.getBoolean(R.styleable.EventView_is_padding, false);
        isSpot = ta.getBoolean(R.styleable.EventView_is_spot, true);
        isDescription = ta.getBoolean(R.styleable.EventView_is_description, false);
        toolbarMode = ta.getBoolean(R.styleable.EventView_toolbar_mode, false);
        belowToolbar = ta.getBoolean(R.styleable.EventView_below_toolbar, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_event, this);

        mainBox = (RelativeLayout) findViewById(R.id.main_box_relative);
        mainLayoutEvent = (LinearLayout) findViewById(R.id.main_layout_event);
        marginToolbarRight = findViewById(R.id.margin_right_toolbar);
        marginToolbarLeft = findViewById(R.id.margin_left_toolbar);
        spotView = (SpotView) findViewById(R.id.spot_view);
        titleLayout = findViewById(R.id.text_relative_layout);
        tvName = (AutofitTextView) findViewById(R.id.title_event);
        tvCountPoints = (SimpleTimerView) findViewById(R.id.places_points);
        tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        smallCategoryIcon = (ImageView) findViewById(R.id.image_small_category_place);
        pointsLayout = findViewById(R.id.points_layout);
        backgroundImage = (ImageView) findViewById(R.id.background_image_event);
        gradientBottomView = findViewById(R.id.bottom_gradient_event);
        gradientBottomViewIfPadding = findViewById(R.id.bottom_gradient_if_padding);
        gradientTopView = findViewById(R.id.topview);
        rvEventTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);
        tagsView = findViewById(R.id.horizontal_tags_view);
        separator = findViewById(R.id.separator);
        descriptionView = findViewById(R.id.description_event);


        marginToolbarRight.setVisibility(VISIBLE);
        marginToolbarLeft.setVisibility(VISIBLE);

        initPadding(isPadding);
        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setToolbarView(toolbarMode);
        setBelowToolbarView(belowToolbar);
    }

    public HorizontalTagsRecyclerView getRvEventTags() {
        return rvEventTags;
    }

    public void setEvent(Place event) {
        this.event = event;

        //Date
        tvTime.setText(event.getTime());

        //Title
        tvName.setText(event.name);

        //EventCategory
        EventCategory eventCategory = null;
        if(colorEvent != -1) {
            Log.d(TAG,"Setting custom color");
            backgroundImage.setImageResource(0);
            mainLayoutEvent.setBackgroundColor(colorEvent);
            if (colorSpot != -1) {
                spotView.setColor(colorSpot);
            }
        } else {
            try {
                Log.d(TAG,"Setting event Background");
                //EventCategory Icon
                eventCategory = MyApplication.getCategoryById(event.category_id);
                categoryIcon.setImageResource(eventCategory.getIconWhiteResId());
                //MyApplication.setCategoryBackground(categoryIcon, event.getLevel());

                //Place background
                backgroundImage.setImageResource(eventCategory.getBigImageResId());
            } catch (UnknownCategoryException e) {
                Log.e(TAG, "no eventCategory found for id : " + event.category_id);
            }
        }

        //Adapter
        rvEventTags.getAdapter().setData(event.tags);

        // Spot view
        if (event.spot != null){
            spotView.setSpot(event.spot);
            this.setSpotVisible(true);
        }
        else{
            this.setSpotVisible(false);
        }

        //Counter
        int initialTime = event.getPoints();
        tvCountPoints.initTimer(initialTime * 1000);
    }


    public void setSpotVisible(boolean isSpot) {
        if(isSpot) {
            spotView.setVisibility(VISIBLE);
        } else {
            spotView.setVisibility(GONE);
            separator.setVisibility(GONE);
        }
    }

    public void setBottomShadow(boolean isVisible) {
        if(isVisible) {
            gradientBottomView.setVisibility(VISIBLE);
        } else {
            gradientBottomView.setVisibility(GONE);
        }
    }

    public void setTopShadow(boolean isVisible) {
        if(isVisible) {
            gradientTopView.setVisibility(VISIBLE);
        } else {
            gradientTopView.setVisibility(GONE);
        }
    }

    public void setTagsVisible(boolean tagsVisibility) {
        if(tagsVisibility && rvEventTags.getAdapter().getData().size()!=0) {
            tagsView.setVisibility(VISIBLE);
        } else {
            tagsView.setVisibility(GONE);
        }
    }

    public void initPadding(boolean isPadding) {
        if (!isPadding) {
            Log.d(TAG, "Removing padding");
            mainBox.setPadding(0, 0, 0, 0);
            gradientBottomViewIfPadding.setVisibility(GONE);
        }
    }

    private void setToolbarView(boolean toolbarMode) {
        if(toolbarMode) {
            mainLayoutEvent.setPadding(0,0,0,0);
            marginToolbarRight.setVisibility(VISIBLE);
            marginToolbarLeft.setVisibility(VISIBLE);
            spotView.setVisibility(GONE);
            gradientBottomView.setVisibility(GONE);
            categoryIcon.setVisibility(GONE);
            smallCategoryIcon.setVisibility(VISIBLE);
            pointsLayout.setVisibility(GONE);
            tagsView.setVisibility(GONE);
            descriptionView.setVisibility(GONE);
        } else {
            setDescriptionView(isDescription);
            setSpotVisible(isSpot);
            setTagsVisible(isTagsVisible);
            marginToolbarRight.setVisibility(GONE);
            marginToolbarLeft.setVisibility(GONE);
            categoryIcon.setVisibility(VISIBLE);
            smallCategoryIcon.setVisibility(GONE);
            pointsLayout.setVisibility(VISIBLE);
        }
    }

    private void setBelowToolbarView(boolean belowToolbar) {
        if(belowToolbar) {
            titleLayout.setVisibility(GONE);
            categoryIcon.setVisibility(GONE);
        }
    }

    public Place getEvent() {
        return event;
    }

    public void setDescriptionView(boolean isDescription) {
        if(isDescription) {
            descriptionView.setVisibility(VISIBLE);
        } else {
            descriptionView.setVisibility(GONE);
        }
    }

    public View getDescriptionView() {
        return descriptionView;
    }

    public void setDescriptionTv(View descriptionTv) {
        this.descriptionView = descriptionTv;
    }
}
