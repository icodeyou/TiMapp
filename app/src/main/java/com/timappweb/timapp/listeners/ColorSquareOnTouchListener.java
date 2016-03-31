package com.timappweb.timapp.listeners;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.R;

public class ColorSquareOnTouchListener implements View.OnTouchListener {


    private final Context context;
    private final TextView tv;

    public ColorSquareOnTouchListener(Context context, TextView tv) {
        this.context = context;
        this.tv = tv;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(R.drawable.background_button_selected);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.setBackgroundResource(R.drawable.background_button);
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_button));
                v.invalidate();
                break;
            }
        }
        return false;
    }
}
