package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventCategory;

/**
 * Created by Jack on 24/02/2016.
 */
public class EventCategoryPagerAdapter extends PagerAdapter {

    private Context mContext;
    private String TAG = "CategoryPager";

    public EventCategoryPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = null;
        try {
            EventCategory eventCategory = MyApplication.getCategoryByIndex(position);
            layout = (ViewGroup) inflater.inflate(eventCategory.getLayoutResId(), collection, false);
        } catch (IndexOutOfBoundsException e) {
            layout = (ViewGroup) inflater.inflate(R.layout.category_unknown, collection, false);
            Log.e(TAG, "Unknown category for pager: " + position);
        }
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return MyApplication.getEventCategories().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        try {
            EventCategory eventCategory = MyApplication.getCategoryByIndex(position);
            return mContext.getResources().getString(eventCategory.getTitleResId());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Unknown category at position: " + position);
            return "Unknown";
        }
    }

}