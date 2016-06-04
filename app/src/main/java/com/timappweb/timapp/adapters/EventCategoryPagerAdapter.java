package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
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

        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.category_image_layout, collection, false);
        TextView nameCategory = (TextView) layout.findViewById(R.id.category_name);
        ImageView imageCategory = (ImageView) layout.findViewById(R.id.category_image);
        try {
            EventCategory eventCategory = ConfigurationProvider.eventCategories().get(position);
            String capitalizedName =
                    eventCategory.getName().substring(0, 1).toUpperCase() + eventCategory.getName().substring(1);
            nameCategory.setText(capitalizedName);
            imageCategory.setImageResource(eventCategory.getBigImageResId());
        } catch (IndexOutOfBoundsException e) {
            String unknownCategory = mContext.getResources().getString(R.string.unknown_category);
            String capitalizedUnknownCategory =
                    unknownCategory.substring(0,1).toUpperCase() + unknownCategory.substring(1);
            nameCategory.setText(capitalizedUnknownCategory);
            imageCategory.setImageResource(R.drawable.image_else);
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
        return ConfigurationProvider.eventCategories().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        try {
            EventCategory eventCategory = ConfigurationProvider.eventCategories().get(position);
            return mContext.getResources().getString(eventCategory.getTitleResId());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Unknown category at position: " + position);
            return "Unknown";
        }
    }

}