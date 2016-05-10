package com.timappweb.timapp.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import com.timappweb.timapp.listeners.SelectableButtonListener;

public class SelectableFloatingButton extends FloatingActionButton {
    private boolean isAbled = false;
    private SelectableButtonListener selectableButtonListener;

    public SelectableFloatingButton(Context context) {
        super(context);
    }

    public SelectableFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectableFloatingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectableListener(SelectableButtonListener selectableListener) {
        this.selectableButtonListener = selectableListener;
    }

    public void switchButton() {
        this.isAbled = !isAbled;
        updateView();
    }

    public void updateView() {
        if(isAbled) {
            selectableButtonListener.onAble();
        } else {
            selectableButtonListener.onDisable();
        }
    }

    public void setState(boolean isActive){
        this.isAbled = isActive;
        updateView();
    }
}
