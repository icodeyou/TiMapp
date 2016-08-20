package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.widget.CompoundButton;

/**
 * Created by Stephane on 20/08/2016.
 */
public class MySwitchCompat extends SwitchCompat{


    private OnCheckedChangeListener mOnCheckChangeListenerCopy;

    public MySwitchCompat(Context context) {
        super(context);
    }

    public MySwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener(listener);
        this.mOnCheckChangeListenerCopy = listener;
    }

    public void setCheckedNoTrigger(boolean checked) {
        setOnCheckedChangeListener(null);
        super.setChecked(checked);
        setOnCheckedChangeListener(mOnCheckChangeListenerCopy);
    }
}
