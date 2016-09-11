package com.timappweb.timapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

//BackCatchEditText is an EditText than intercepts the event when the keyboard
//is hidden by pressing the android back key.
// Though in AddEventActivity, the event has been managed thanks to the method setOnFocusChangedListener

public class BackCatchEditText extends EditText {


    private HandleDismissingKeyboard handleDismissingKeyboard;

    public BackCatchEditText(Context context) {
        super(context);
    }

    public BackCatchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackCatchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface HandleDismissingKeyboard {
        public void dismissKeyboard();
    }

    public void setHandleDismissingKeyboard(
            HandleDismissingKeyboard HandleDismissingKeyboard) {
        this.handleDismissingKeyboard = HandleDismissingKeyboard;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            handleDismissingKeyboard.dismissKeyboard();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
