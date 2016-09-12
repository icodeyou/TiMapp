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
import com.timappweb.timapp.data.models.Event;

/**
 * Created by stephane on 4/6/2016.
 */
public abstract class EventBaseFragment extends BaseFragment {

    protected EventActivity eventActivity;
    protected OnCreateViewCallback createViewCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventActivity = (EventActivity) getActivity();
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(createViewCallback!=null) {
            createViewCallback.onCreateView();
        }
    }

    public Event getEvent() {
        return eventActivity.getEvent();
    }

    public interface OnCreateViewCallback {

        void onCreateView();

    }

    public void setCreateViewCallback(OnCreateViewCallback createViewCallback) {
        this.createViewCallback = createViewCallback;
    }


}
