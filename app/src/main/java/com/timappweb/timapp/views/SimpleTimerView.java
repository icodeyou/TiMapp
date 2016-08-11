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

    public void initTimer(int initialms) {
        final TextView currentTextView = this;
        if(countDownTimer==null) {
            countDownTimer = new CountDownTimer(initialms*1000, COUNTDOWNINTERVAL) {

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

    public void cancelTimer() {
        if(countDownTimer!=null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public int getPoints() {
        int points;
        if(this.getText() == getResources().getString(R.string.counter_over)) {
            points = 0;
        } else {
            points = Integer.parseInt(this.getText().toString());
        }
        return points;
    }
}
