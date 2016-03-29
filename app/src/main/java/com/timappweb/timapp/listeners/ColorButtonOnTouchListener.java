package com.timappweb.timapp.listeners;

import android.view.MotionEvent;
import android.view.View;

import com.timappweb.timapp.R;

/**
 * Created by stephane on 3/29/2016.
 */
public class ColorButtonOnTouchListener implements View.OnTouchListener {

    private final int color;

    public ColorButtonOnTouchListener() {
        this.color = R.color.colorSecondary;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(color);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.setBackground(null);
                v.invalidate();
                break;
            }
        }
        return false;
    }
}
