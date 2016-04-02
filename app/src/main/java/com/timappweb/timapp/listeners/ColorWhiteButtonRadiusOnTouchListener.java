package com.timappweb.timapp.listeners;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.R;

public class ColorWhiteButtonRadiusOnTouchListener implements View.OnTouchListener {

    private final Context context;
    private final TextView tv;

    public ColorWhiteButtonRadiusOnTouchListener(Context context, TextView tv) {
        this.context = context;
        this.tv = tv;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionIndex()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(R.drawable.background_button_selected);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                v.invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                v.setBackgroundResource(R.drawable.background_white_button);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                v.invalidate();
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                v.setBackgroundResource(R.drawable.background_white_button);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                v.invalidate();
                return true;
            }
        }
        return false;
    }
}