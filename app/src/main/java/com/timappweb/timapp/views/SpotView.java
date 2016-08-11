package com.timappweb.timapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.LayoutSpotBinding;
import com.timappweb.timapp.utils.Util;

import cdflynn.android.library.crossview.CrossView;

public class SpotView extends LinearLayout {

    private Context context;
    private final boolean center;
    private LayoutSpotBinding mBinding;


    public SpotView(Context context) {
        super(context);
        center = false;
        this.init(context);
    }

    public SpotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpotView, 0, 0);
        center = ta.getBoolean(R.styleable.SpotView_center, false);

        this.init(context);
    }

    private void init(Context context) {
        this.context = context;

        //LayoutInflater inflater = (LayoutInflater)
        //        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(
                context), R.layout.layout_spot, this, false);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.horizontal_linear_layout);
        TextView titleSpot = (TextView) findViewById(R.id.title_spot);

        if(center) {
            int wrapContent= LayoutParams.WRAP_CONTENT;
            mainLayout.getLayoutParams().width = wrapContent;
            titleSpot.getLayoutParams().width = wrapContent;
        }
    }

    public void setSpot(Spot spot) {
        mBinding.setSpot(spot);
    }

}
