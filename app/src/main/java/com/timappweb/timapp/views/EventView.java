package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
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
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import java.util.List;

import me.grantland.widget.AutofitTextView;

public class EventView extends RelativeLayout{
    private final static String TAG = "EventView";
    private Context context;

    private Place event;

    private View                        tagsView;
    private AutofitTextView             tvName;
    private TextView                    tvTime;
    private FrameLayout                 tagsFrameLayout;
    private ImageView                   categoryIcon;
    private ImageView                   smallCategoryIcon;
    private View                        pointsLayout;
    private ImageView                   backgroundImage;
    private SimpleTimerView             tvCountPoints;
    private SimpleTimerView             tvCountPointsWhite;
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

    private int                         colorSpot;
    private int                         colorEvent;
    private boolean                     isTagsVisible;
    private boolean                     isBottomShadow;
    private boolean                     isTopShadow;
    private boolean                     isPadding;
    private boolean                     isSpot;
    private boolean                     isDescription;
    private boolean                     toolbarMode;
    private boolean                     isBelowToolbarView;
    private HorizontalTagsAdapter       htAdapter;
    private HorizontalTagsRecyclerView  htrv;
    private View                        iamhereButton;
    private ImageView                   matchIcon;
    private TextView                    iamhereText;
    private View                        whitePointsLayout;


    public EventView(Context context) {
        super(context);
        this.context = context;
        this.isSpot = false;
        this.isDescription = true;
        this.colorEvent = ContextCompat.getColor(context, R.color.background_half_black);
        this.isBelowToolbarView = false;
        this.init();
    }

    public EventView(Context context, boolean isBelowToolbarView) {
        super(context);
        this.context = context;
        this.isSpot = false;
        this.isDescription = true;
        this.colorEvent = ContextCompat.getColor(context, R.color.background_half_black);
        this.isBelowToolbarView = isBelowToolbarView;
        this.init();
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

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
        isBelowToolbarView = ta.getBoolean(R.styleable.EventView_show_title, false);
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
        tvCountPointsWhite = (SimpleTimerView) findViewById(R.id.white_points_text);
        tvName = (AutofitTextView) findViewById(R.id.title_event);
        tvCountPoints = (SimpleTimerView) findViewById(R.id.places_points);
        tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        smallCategoryIcon = (ImageView) findViewById(R.id.image_small_category_place);
        pointsLayout = findViewById(R.id.points_layout);
        backgroundImage = (ImageView) findViewById(R.id.background_image_event);
        gradientBottomView = findViewById(R.id.bottom_gradient_event);
        //gradientBottomViewIfPadding = findViewById(R.id.bottom_gradient_if_padding);
        tagsView = findViewById(R.id.horizontal_tags_view);
        separator = findViewById(R.id.separator);
        descriptionView = findViewById(R.id.description_event);
        descriptionTv = (TextView) findViewById(R.id.description_textview);
        iamhereButton = findViewById(R.id.iamhere_button);
        iamhereText = (TextView) findViewById(R.id.iamhere_text);
        matchIcon = (ImageView) findViewById(R.id.match_icon);

        iamhereButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                matchIcon.setImageResource(R.drawable.match_red);
                iamhereText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
        });

        tagsFrameLayout = (FrameLayout) findViewById(R.id.htrv_frame_layout);

        //htAdapter = (HorizontalTagsAdapter) rvEventTags.getAdapter();



        if(isSpot) {
            gradientTopView = findViewById(R.id.topview_no_spot);
        } else {
            gradientTopView = findViewById(R.id.topview_with_spot);
        }

        //initPadding();
        setBottomShadow();
        setTopShadow();
        setSpotVisible(isSpot);
        setDescriptionView(isDescription);
        //setTagsVisible(isTagsVisible);
        setToolbarView();
        setBelowToolbarView();
    }

    public HorizontalTagsRecyclerView getRvEventTags() {
        return htrv;
    }

    public HorizontalTagsRecyclerView setEvent(Place event) {
        //TODO : CLEAR

        this.event = event;

        //Date
        tvTime.setText(event.getTime());

        //Title
        tvName.setText(event.name);

        if(event.description!=null) {
            descriptionTv.setText(event.description);
        } else {
            descriptionTv.setVisibility(GONE);
        }

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

        //Tags Adapter
        List<Tag> tags = event.tags;
        if(tags!=null) {
            htrv = new HorizontalTagsRecyclerView(context,tags);
            tagsFrameLayout.removeAllViews();
            tagsFrameLayout.addView(htrv);
            if(isTagsVisible && tags.size()!=0) {
                tagsFrameLayout.setVisibility(VISIBLE);
            } else {
                tagsFrameLayout.setVisibility(GONE);
            }
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
        tvCountPointsWhite.initTimer(initialTime * 1000);

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

    private void setBottomShadow() {
        if(isBottomShadow) {
            gradientBottomView.setVisibility(VISIBLE);
        } else {
            gradientBottomView.setVisibility(GONE);
        }
    }

    private void setTopShadow() {
        if(isTopShadow) {
            gradientTopView.setVisibility(VISIBLE);
        } else {
            gradientTopView.setVisibility(GONE);
        }
    }

    /*private void initPadding() {
        if (!isPadding) {
            Log.d(TAG, "Removing padding");
            mainBox.setPadding(0, 0, 0, 0);
            gradientBottomViewIfPadding.setVisibility(GONE);
        }
    }*/

    private void setToolbarView() {
        if(toolbarMode) {
            mainLayoutEvent.setPadding(0, 0, 0, 0);
            marginToolbarRight.setVisibility(VISIBLE);
            marginToolbarLeft.setVisibility(VISIBLE);
            spotView.setVisibility(GONE);
            categoryIcon.setVisibility(GONE);
            smallCategoryIcon.setVisibility(VISIBLE);
            pointsLayout.setVisibility(GONE);
            tagsFrameLayout.setVisibility(GONE);
            descriptionView.setVisibility(GONE);
        }
    }

    private void setBelowToolbarView() {
        if(isBelowToolbarView) {
            titleLayout.setVisibility(GONE);
            categoryIcon.setVisibility(GONE);
            smallCategoryIcon.setVisibility(GONE);
            pointsLayout.setVisibility(GONE);
            whitePointsLayout.setVisibility(VISIBLE);
        }
    }

    public Place getEvent() {
        return event;
    }

    public void setDescriptionView(boolean isDescription) {
        if(isDescription) {
            if(descriptionTv!=null)
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
