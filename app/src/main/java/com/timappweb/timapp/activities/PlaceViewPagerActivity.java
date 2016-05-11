package com.timappweb.timapp.activities;

import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.views.MyHackyViewPager;

import me.relex.photodraweeview.PhotoDraweeView;


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

    /*@Override
    public void onBackPressed() {
        if (page.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            page.setCurrentItem(page.getCurrentItem() - 1);
        }
    }*/

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
            final PhotoDraweeView photoDraweeView = new PhotoDraweeView(container.getContext());
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(Uri.parse(IMAGES[position]));
            controller.setOldController(photoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            photoDraweeView.setController(controller.build());

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoDraweeView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return IMAGES.length;
        }
    }


}