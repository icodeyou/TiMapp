package com.timappweb.timapp.views;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.TextView;

import com.timappweb.timapp.R;

public class SimpleTimerView extends TextView{
    private static int COUNTDOWNINTERVAL = 1000;
    private CountDownTimer countDownTimer;

    public SimpleTimerView(Context context) {
        super(context);
    }

    public SimpleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initTimer(int initialTime) {
        final TextView currentTextView = this;
        if(countDownTimer==null) {
            countDownTimer = new CountDownTimer(initialTime, COUNTDOWNINTERVAL) {

                public void onTick(long millisUntilFinished) {
                    currentTextView.setText(String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    currentTextView.setText(R.string.counter_over);
                }
            };
            countDownTimer.start();
        }
    }
}
