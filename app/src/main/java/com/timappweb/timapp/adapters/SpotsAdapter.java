package com.timappweb.timapp.adapters;

import android.content.Context;

import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.SpotItem;
import com.timappweb.timapp.data.models.Spot;

public class SpotsAdapter extends MyFlexibleAdapter {

    private static final String TAG = "SpotsAdapter";

    public SpotsAdapter(Context context) {
        super(context);
    }

    public Spot getSpot(int position) {
        return ((SpotItem)getItem(position)).getSpot();
    }

}