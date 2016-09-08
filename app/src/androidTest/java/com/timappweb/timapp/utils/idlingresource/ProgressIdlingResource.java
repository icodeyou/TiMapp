package com.timappweb.timapp.utils.idlingresource;

import android.app.Activity;
import android.support.test.espresso.IdlingResource;
import android.view.View;
import android.widget.ProgressBar;

import com.timappweb.timapp.utils.ActivityHelper;

/**
 * Created by Stephane on 08/09/2016.
 */
public class ProgressIdlingResource implements IdlingResource {

    private final View progressBar;
    private ResourceCallback resourceCallback;

    public ProgressIdlingResource(Activity activity, final View progressBar){
        this.progressBar = progressBar;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if (ProgressIdlingResource.this.isIdleNow()){
                            if (resourceCallback == null){
                                return ;
                            }
                            //Called when the resource goes from busy to idle.
                            resourceCallback.onTransitionToIdle();
                        }
                    }
                });
            }
        });
    }


    @Override
    public String getName() {
        return "Progress Idling resource";
    }

    @Override
    public boolean isIdleNow() {
        // the resource becomes idle when the progress has been dismissed
        if (!progressBar.isShown()){
            if (resourceCallback != null) {
                resourceCallback.onTransitionToIdle();
            }
            return true;
        }
        return false;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}