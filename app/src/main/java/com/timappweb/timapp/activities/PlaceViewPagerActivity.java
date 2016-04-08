package com.timappweb.timapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.views.MyHackyViewPager;

import uk.co.senab.photoview.PhotoView;

public class PlaceViewPagerActivity extends FragmentActivity {

    private static String[] IMAGES;

    private ViewPager page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMAGES = IntentsUtils.extractPicture(getIntent());
        if (IMAGES == null){
            IntentsUtils.home(this);
        }
        setContentView(R.layout.activity_view_pager2);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter();
        page = (MyHackyViewPager)findViewById(R.id.view_pager);
        page.setAdapter(pagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (page.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            page.setCurrentItem(page.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private class ScreenSlidePagerAdapter extends PagerAdapter {

        /*public ScreenSlidePagerAdapter() {
            return ;
        }*/

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView photoView = new SimpleDraweeView(container.getContext());

            Uri uri = Uri.parse(IMAGES[position]);
            photoView.setImageURI(uri);

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /*@Override
        public Fragment getItem(int position) {
            ArchiveViewPagerFragment fragment = new ArchiveViewPagerFragment();
            fragment.setAsset(IMAGES[position]);
            return fragment;
        }*/

        @Override
        public int getCount() {
            return IMAGES.length;
        }
    }


}