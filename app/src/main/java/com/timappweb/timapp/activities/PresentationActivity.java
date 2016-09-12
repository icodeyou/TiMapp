package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;

public class PresentationActivity extends BaseActivity {
    private String          TAG                     = "PresentationActivity";

    private ViewPager viewPager;
    private View skipButton;
    private View nextButton;
    private View loginButton;

    //source :
    //https://www.bignerdranch.com/blog/viewpager-without-fragments/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        setStatusBarColor(R.color.black);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PresentationPagerAdapter(this));
        skipButton = findViewById(R.id.skip_button);
        nextButton = findViewById(R.id.next_button);

        initListeners();
    }

    private void initListeners() {
        final Activity that = this;

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.home(that);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, viewPager.getCurrentItem()+"");
                Log.d(TAG, viewPager.getAdapter().getCount()+"");
                if(viewPager.getCurrentItem()!=viewPager.getAdapter().getCount()-1) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
            }
        });
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

            if(position==viewPager.getAdapter().getCount()-1) {
                View letsgetitstarted = layout.findViewById(R.id.final_button);
                letsgetitstarted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentsUtils.login(getBaseContext());
                    }
                });
            }

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
