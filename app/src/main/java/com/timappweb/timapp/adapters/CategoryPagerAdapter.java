package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;

/**
 * Created by Jack on 24/02/2016.
 */
public class CategoryPagerAdapter extends PagerAdapter {

    private Context mContext;
    private String TAG = "CategoryPager";

    public CategoryPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = null;
        try {
            Category category = MyApplication.getCategoryByIndex(position);
            layout = (ViewGroup) inflater.inflate(category.getLayoutResId(), collection, false);
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
        return MyApplication.getCategories().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        try {
            Category category = MyApplication.getCategoryByIndex(position);
            return mContext.getResources().getString(category.getTitleResId());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Unknown category at position: " + position);
            return "Unknown";
        }
    }

}