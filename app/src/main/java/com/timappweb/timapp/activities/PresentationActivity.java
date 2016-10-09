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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.Util;

public class PresentationActivity extends BaseActivity {
    private String          TAG                     = "PresentationActivity";

    private ViewPager viewPager;
    private View skipButton;
    private View nextButton;
    private View loginButton;
    private View buttonsBottom;

    private int previousPosition;

    private PresentationActivity activity;

    //source :
    //https://www.bignerdranch.com/blog/viewpager-without-fragments/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        Util.setStatusBarColor(this, R.color.black);

        initViewPager();
        skipButton = findViewById(R.id.skip_button);
        nextButton = findViewById(R.id.next_button);
        buttonsBottom = findViewById(R.id.buttons_layout);
        activity = this;

        initListeners();
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PresentationPagerAdapter(this));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int lastPage = viewPager.getAdapter().getCount()-1;
                if(position==lastPage) {
                    hideButtons();
                } else if(position==lastPage-1 && previousPosition==lastPage) {
                    displayButtons();
                }

                previousPosition = viewPager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initListeners() {
        final Activity that = this;

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.getBackToParent(that);
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

    private void hideButtons() {
        Animation hideButtons = AnimationUtils.loadAnimation(this, R.anim.slide_out_down);
        hideButtons.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonsBottom.setVisibility(View.INVISIBLE);
                //buttonsBottom.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        buttonsBottom.startAnimation(hideButtons);
    }

    private void displayButtons() {
        Animation displayButtons = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        buttonsBottom.startAnimation(displayButtons);
        buttonsBottom.setVisibility(View.VISIBLE);
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
                        IntentsUtils.loginOrBackToParent(activity);
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
