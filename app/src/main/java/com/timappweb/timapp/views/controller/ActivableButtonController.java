package com.timappweb.timapp.views.controller;

import android.view.View;

/**
 * Created by stephane on 5/26/2016.
 */
public abstract class ActivableButtonController extends ButtonStateController {

    private boolean mPendingState;

    // =============================================================================================

    protected abstract boolean performActivate();
    protected abstract boolean cancelActivate();

    // =============================================================================================

    public ActivableButtonController(View view) {
        super(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mView.isActivated()) {
                    mPendingState = true;
                    if (performActivate()) {
                        mView.setActivated(true);
                    }
                } else {
                    mPendingState = false;
                    if (cancelActivate()) {
                        mView.setActivated(false);
                    }
                }
            }
        });
    }

    protected void rollbackChange() {
        mView.setActivated(mPendingState);
    }

    protected void commitChange() {
        mView.setActivated(!mPendingState);
    }

    public void setActivated(boolean activated) {
        mView.setActivated(activated);
    }

}
