package com.timappweb.timapp.utils;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by stephane on 1/31/2016.
 */
public class EachSecondTimerTask {

    private static Timer timer = null;
    //private static List<EachSecondTimerTask> instances = new LinkedList<>();

    private final TimerTask task;

    private EachSecondTimerTask(final TimeTaskCallback timeTaskCallback) {

        task = new TimerTask() {
            private Handler mHandler = new Handler();
            @Override
            public void run() {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                timeTaskCallback.update();
                            }
                        });
                    }
                }).start();
            }
        };

        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void cancel() {
        this.task.cancel();
    }

    public static EachSecondTimerTask add(TimeTaskCallback timeTaskCallback) {
        if (timer == null){
            timer = new Timer();
        }
        EachSecondTimerTask task = new EachSecondTimerTask(timeTaskCallback);
        return task;
    }


    /*
    public static void clear(){
        if (instances.size() > 0){
            for (EachSecondTimerTask eachSecondTimerTask: instances){
                eachSecondTimerTask.cancel();
            }
            instances.clear();
        }
    }

    public static EachSecondTimerTask create(TimeTaskCallback timeTaskCallback) {
        clear();
        return add(timeTaskCallback);
    }*/

}
