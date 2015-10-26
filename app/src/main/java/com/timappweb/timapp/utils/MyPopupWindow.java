package com.timappweb.timapp.utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.timappweb.timapp.R;

/**
 * Created by stephane on 8/30/2015.
 */
public class MyPopupWindow {

    private static final String TAG = "PopupWindow";
    private Activity activity;

    MyPopupWindow(Activity act){
        this.activity = act;
    }

    public void showPopup(View anchorView) {

        View popupView = activity.getLayoutInflater().inflate(R.layout.fragment_spotstag_list, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);

        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);
        Log.d(TAG, "Anchor view position for popup: " + location[0] + "-" + location[1]);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY,   location[0], location[1] + anchorView.getHeight());

    }
}
