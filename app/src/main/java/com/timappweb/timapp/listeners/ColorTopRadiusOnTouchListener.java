package com.timappweb.timapp.listeners;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.R;

import org.w3c.dom.Text;

public class ColorTopRadiusOnTouchListener implements View.OnTouchListener {


    private final Context context;
    private final TextView tv;

    public ColorTopRadiusOnTouchListener(Context context, TextView tv) {
        this.context = context;
        this.tv = tv;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                tv.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));
                v.setBackgroundResource(R.drawable.background_radius_selected);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_button));
                v.setBackground(null);
                v.invalidate();
                break;
            }
        }
        return false;
    }
}
