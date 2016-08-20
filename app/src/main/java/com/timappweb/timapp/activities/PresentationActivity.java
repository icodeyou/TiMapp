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

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PresentationPagerAdapter(this));
    }

    public enum CustomPagerEnum {

        RED(R.string.text_anonymous, R.layout.presentation_one),
        BLUE(R.string.title_activity_profile, R.layout.presentation_two),
        ORANGE(R.string.title_activity_invite_friends, R.layout.presentation_three);

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
