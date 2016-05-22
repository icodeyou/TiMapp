package com.timappweb.timapp.views.parallaxviewpager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ScrollView;

import com.timappweb.timapp.fragments.BaseFragment;

/**
 * Created by desmond on 12/4/15.
 */
public class ScrollTabHolderFragment extends BaseFragment implements ScrollTabHolder {

    protected static final String ARG_POSITION = "position";

    protected ScrollTabHolder mScrollTabHolder;
    protected int mPosition;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mScrollTabHolder = (ScrollTabHolder) activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ScrollTabHolder");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPosition = getArguments().getInt(ARG_POSITION);
        return null;
    }

    @Override
    public void onDetach() {
        mScrollTabHolder = null;
        super.onDetach();
    }

    @Override
    public void adjustScroll(int scrollHeight, int headerHeight) {}

    @Override
    public void onListViewScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount, int pagePosition) {}

    @Override
    public void onScrollViewScroll(ScrollView view, int x, int y, int oldX, int oldY, int pagePosition) {}

    @Override
    public void onRecyclerViewScroll(RecyclerView view, int dx, int dy, int scrollY, int pagePosition) {}
}
