package com.timappweb.timapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.adapters.EventCategoriesAdapter;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import cdflynn.android.library.crossview.CrossView;

public class CategorySelectorView extends LinearLayout {

    private Context context;
    private static final int CATEGORIES_COLUMNS = 4;
    private static final int NUMBER_OF_MAIN_CATEGORIES = 4;

    private OnClickListener displayHideCategories;
    private InputMethodManager  imm;
    //Views
    private RecyclerView        rvAllCategories;
    private RecyclerView        rvMainCategories;
    private CrossView           moreBtn;
    private TextView            pickTv;
    private ImageView           imageSelectedCategory;
    private TextView            textSelectedCategory;
    private View                selectedCategoryView;


    public CategorySelectorView(Context context) {
        super(context);
        this.init(context);
    }

    public CategorySelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        //TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpotView, 0, 0);
        //isTagsVisible = ta.getBoolean(R.styleable.CardView_tags_visible, false);

        this.init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(getContext(), R.layout.view_category_selector, this);

        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        rvAllCategories = (RecyclerView) findViewById(R.id.rv_all_categories);
        rvMainCategories = (RecyclerView) findViewById(R.id.rv_main_categories);
        imageSelectedCategory = (ImageView) findViewById(R.id.image_category_selected);
        textSelectedCategory = (TextView) findViewById(R.id.text_category_selected);
        selectedCategoryView = findViewById(R.id.selected_category);
        moreBtn = (CrossView) findViewById(R.id.more_button);
        pickTv = (TextView) findViewById(R.id.pick_tv);

        initRv();
        initListeners();
    }

    private void initListeners() {
        displayHideCategories = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreBtn.toggle();
                if(!isExpandedView()) {
                    final Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_down_all);
                    rvAllCategories.startAnimation(slideIn);
                    pickTv.setVisibility(View.VISIBLE);
                    rvAllCategories.setVisibility(View.VISIBLE);
                    rvMainCategories.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);   //Hide keyboard
                } else {
                    pickTv.setVisibility(View.GONE);
                    rvAllCategories.setVisibility(View.GONE);
                    if(selectedCategoryView.getVisibility()==GONE) {
                        final Animation slideMainIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_down_main);
                        rvMainCategories.startAnimation(slideMainIn);
                        rvMainCategories.setVisibility(View.VISIBLE);
                    }
                }
            }
        } ;

        moreBtn.setOnClickListener(displayHideCategories);
        selectedCategoryView.setOnClickListener(displayHideCategories);
    }

    private void initRv() {
        //MAIN
        GridLayoutManager mainManager = new GridLayoutManager(context,
                NUMBER_OF_MAIN_CATEGORIES,LinearLayoutManager.VERTICAL,false);
        rvMainCategories.setLayoutManager(mainManager);

        //ALL
        GridLayoutManager allManager = new GridLayoutManager(context,
                CATEGORIES_COLUMNS, LinearLayoutManager.VERTICAL,false);
        rvAllCategories.setLayoutManager(allManager);
    }

    public void setAdapters(EventCategoriesAdapter adapterMain, EventCategoriesAdapter adapterAll) {
        rvMainCategories.setAdapter(adapterMain);
        rvAllCategories.setAdapter(adapterAll);
    }

    public boolean isExpandedView() {
        return rvAllCategories.getVisibility()==VISIBLE;
    }

    public void expandView() {

    }

    public void lowerView() {

    }

    public void selectCategoryUI(String name, int iconResId) {
        imageSelectedCategory.setImageResource(iconResId);
        textSelectedCategory.setText(name);
        selectedCategoryView.setVisibility(View.VISIBLE);
        rvMainCategories.setVisibility(View.GONE);
    }

}