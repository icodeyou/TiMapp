package com.timappweb.timapp.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class HorizontalTagsTouchListener implements View.OnTouchListener {
    private OnItemAdapterClickListener mListener;
    private GestureDetector mGestureDetector;
    final private static String TAG = "HorTagsTouchListener";

    public interface OnItemClickListener {
        void onItemClick(RecyclerView recyclerView, View view, int position);
    }

    public HorizontalTagsTouchListener(Context context, final OnItemAdapterClickListener listener,
                                       final int position ) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                listener.onClick(position);
                Log.i(TAG,"Single Tap Up");
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return false;
    }
}