package com.timappweb.timapp.views.controller;

import android.view.View;

/**
 * Created by stephane on 5/26/2016.
 */
public abstract class ButtonStateController {

    public final View mView;

    // =============================================================================================

    public ButtonStateController(View view){
        mView = view;
    }

    public abstract void initState();

}
