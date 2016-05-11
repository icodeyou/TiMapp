package com.timappweb.timapp.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.timappweb.timapp.listeners.SelectableButtonListener;

public class SelectableFloatingButton extends FloatingActionButton {
    private boolean stateOn = false;
    private SelectableButtonListener selectableButtonListener;

    public SelectableFloatingButton(Context context) {
        super(context);
        this.init();
    }

    public SelectableFloatingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public SelectableFloatingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init(){
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnabled(false);
                performAction();
            }
        });
    }

    public void setSelectableListener(SelectableButtonListener selectableListener) {
        this.selectableButtonListener = selectableListener;
    }

    public void performAction() {
        if(!stateOn) {
            if (selectableButtonListener.performEnabled()){
                stateOn = true;
                selectableButtonListener.updateUI(this.stateOn);
            }
        } else {
            if (selectableButtonListener.performDisabled()){
                stateOn = false;
                selectableButtonListener.updateUI(this.stateOn);
            }
        }
    }

    public void updateUI() {
        selectableButtonListener.updateUI(this.stateOn);
    }

    public void setStateOn(boolean stateOn) {
        this.stateOn = stateOn;
        updateUI();
    }
}
