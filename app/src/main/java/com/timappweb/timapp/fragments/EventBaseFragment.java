package com.timappweb.timapp.fragments;



import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.activities.EventActivity;

/**
 * Created by stephane on 4/6/2016.
 */
public abstract class EventBaseFragment extends BaseFragment {

    protected EventActivity eventActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventActivity = (EventActivity) getActivity();
        return null;
    }


}
