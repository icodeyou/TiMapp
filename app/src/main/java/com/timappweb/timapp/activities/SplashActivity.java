package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;

/**
 * Created by stephane on 3/26/2016.
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity context = this;

        MyApplication.ready()
                .done(new DoneCallback() {
                    @Override
                    public void onDone(Object result) {
                        if (MyApplication.isLoggedIn()){
                            IntentsUtils.home(context);
                        }
                        else{
                            IntentsUtils.login(context);
                        }
                        finish();
                    }
                })
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(Object progress) {
                        Log.i(TAG, "Progressing: " + progress);
                    }
                })
                .fail(new FailCallback() {
                    @Override
                    public void onFail(Object result) {
                        IntentsUtils.error(context);
                        finish();
                    }
                });
    }

}
