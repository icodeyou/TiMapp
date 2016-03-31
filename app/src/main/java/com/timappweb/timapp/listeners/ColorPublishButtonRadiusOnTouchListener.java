package com.timappweb.timapp.listeners;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.R;

public class ColorPublishButtonRadiusOnTouchListener implements View.OnTouchListener {


    private final Context context;
    private final TextView tv;
    private final TextView tv2;

    public ColorPublishButtonRadiusOnTouchListener(Context context, TextView tv, TextView tv2) {
        this.context = context;
        this.tv = tv;
        this.tv2 = tv2;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(R.drawable.background_radius_selected);
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                tv2.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.setBackground(null);
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_button));
                tv2.setTextColor(ContextCompat.getColor(context, R.color.text_button));
                v.invalidate();
                break;
            }
        }
        return false;
    }

}
