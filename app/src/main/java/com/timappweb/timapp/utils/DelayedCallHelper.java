package com.timappweb.timapp.utils;

/**
 * Created by Stephane on 13/09/2016.
 */
public class DelayedCallHelper {

    public static Thread create(final long delay, final Callback callback){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(delay); // As I am using LENGTH_LONG in Toast
                    callback.onTime();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };
        thread.start();
        return thread;
    }

    public interface Callback{
        void onTime();
    }
}
