package com.timappweb.timapp.listeners;

import android.view.MotionEvent;
import android.view.View;

import com.timappweb.timapp.R;

public class ColorAllOnTouchListener implements View.OnTouchListener {

    private final int color;

    public ColorAllOnTouchListener() {
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
