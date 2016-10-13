package com.timappweb.timapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;


@BindingMethods(@BindingMethod(type = SimpleTimerView.class, attribute = "myattrs:initialTime", method = "initBindingTime"))
public class SimpleTimerView extends TextSwitcher {
    private static int COUNTDOWNINTERVAL = 1000;
    private static final long TIMELAPSE_HOT_ANIM = 2000;
    private static final String TAG = "SimpleTimerView";

    private CountDownTimer countDownTimer;
    private int remainingSeconds;

    public SimpleTimerView(Context context) {
        super(context);
        init();
    }

    public SimpleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        //TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SimpleTimerView, 0, 0);
        //initialTimeMs = ta.getInteger(R.styleable.SimpleTimerView_initialTime, -1);

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

    public void initTimer(long initialTimeSec) {
        Log.d(TAG, "Inializing timer");
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
        if(MyApplication.getApplicationBaseContext().getResources().getBoolean(R.bool.event_animateCountDownTimer)
                && initialTimeSec > 0) {
            countDownTimer = new CountDownTimer(initialTimeSec *1000, COUNTDOWNINTERVAL) {

                public void onTick(long millisUntilFinished) {
                    remainingSeconds = (int) (millisUntilFinished / 1000);
                    SimpleTimerView.this.setText(String.valueOf(remainingSeconds));
                }

                public void onFinish() {
                    SimpleTimerView.this.setText(getResources().getString(R.string.counter_over));
                }
            };
            countDownTimer.start();
        }
    }

    public void cancelTimer() {
        if(countDownTimer != null) {
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

    public void setText(int string) {
        setText(this.getResources().getString(string));
    }

    public void animPointsTo(int newPoints) {
        ValueAnimator animator = new ValueAnimator();
        cancelTimer();

        int initialPoints;
        if(remainingSeconds>0) {
            initialPoints = remainingSeconds;
        }
        else {
            initialPoints = 0;
        }

        final int pointsAdded = newPoints - getPoints();
        int finalPoints = (int) (initialPoints + pointsAdded - TIMELAPSE_HOT_ANIM/1000);

        Log.d(TAG, "Initial points : " + initialPoints + ". Final points : " + finalPoints);
        animator.setObjectValues(initialPoints, finalPoints);
        animator.setDuration(TIMELAPSE_HOT_ANIM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.start();
        if(finalPoints<=0) {
            cancelTimer();
            Log.d(TAG, "Set timer text to Over");
            setText(getResources().getString(R.string.counter_over));
        } else {
            Log.d(TAG, "Initializing timer to " + finalPoints);
            initTimer(finalPoints);
        }
    }
}