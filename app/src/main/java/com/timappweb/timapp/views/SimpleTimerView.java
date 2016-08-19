package com.timappweb.timapp.views;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.timappweb.timapp.R;

public class SimpleTimerView extends TextSwitcher {
    private static int COUNTDOWNINTERVAL = 1000;
    private CountDownTimer countDownTimer;

    public SimpleTimerView(Context context) {
        super(context);
        init();
    }

    public SimpleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setFactory(new ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from((getContext()));
                TextView textView = (TextView) inflater.inflate(R.layout.textview_counter, null);
                return textView;
            }
        });

        Animation inAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.timer_in);
        Animation outAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.timer_out);
        this.setInAnimation(inAnimation);
        this.setOutAnimation(outAnimation);
    }

    public void initTimer(int initialms) {
        final TextSwitcher currentTextView = this;
        if(countDownTimer==null) {
            countDownTimer = new CountDownTimer(initialms*1000, COUNTDOWNINTERVAL) {

                public void onTick(long millisUntilFinished) {
                    currentTextView.setText(String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    currentTextView.setText(getResources().getString(R.string.counter_over));
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
        TextView textView = (TextView) this.getCurrentView();
        String text = textView.getText().toString();
        int points;
        if(text == getResources().getString(R.string.counter_over)) {
            points = 0;
        } else {
            points = Integer.parseInt(text);
        }
        return points;
    }

}
