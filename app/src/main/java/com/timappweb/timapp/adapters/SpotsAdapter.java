package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.SpotItem;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.ActivityAddEventBinding;
import com.timappweb.timapp.databinding.LayoutSpotBinding;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SpotsAdapter extends MyFlexibleAdapter {

    private static final String TAG = "SpotsAdapter";

    public SpotsAdapter(Context context) {
        super(context);
    }

    public Spot getSpot(int position) {
        return ((SpotItem)getItem(position)).getSpot();
    }

}