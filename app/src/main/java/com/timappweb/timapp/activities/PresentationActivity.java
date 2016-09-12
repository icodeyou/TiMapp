package com.timappweb.timapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;

public class PresentationActivity extends BaseActivity {
    private String          TAG                     = "EventActivity";

    private ViewPager viewPager;

    //source :
    //https://www.bignerdranch.com/blog/viewpager-without-fragments/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        setStatusBarColor(R.color.black);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PresentationPagerAdapter(this));


        initListeners();
    }

    private void initListeners() {

    }

    public enum CustomPagerEnum {

        ONE(R.string.p_key_one, R.layout.presentation_one),
        TWO(R.string.p_key_two, R.layout.presentation_two),
        THREE(R.string.p_key_three, R.layout.presentation_three),
        FOUR(R.string.p_key_four, R.layout.presentation_four),
        FIVE(R.string.p_key_five, R.layout.presentation_five),
        SIX(R.string.p_key_six, R.layout.presentation_six),
        SEVEN(R.string.p_key_seven, R.layout.presentation_seven);

        private int mTitleResId;
        private int mLayoutResId;

        CustomPagerEnum(int titleResId, int layoutResId) {
            mTitleResId = titleResId;
            mLayoutResId = layoutResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public int getLayoutResId() {
            return mLayoutResId;
        }

    }

    private class PresentationPagerAdapter extends PagerAdapter {
        private Context mContext;

        public PresentationPagerAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), container, false);
            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return CustomPagerEnum.values().length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
            return mContext.getString(customPagerEnum.getTitleResId());
        }
    }
}
