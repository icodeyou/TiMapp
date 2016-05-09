package com.timappweb.timapp.listeners;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.model.RestFeedback;

import retrofit2.Call;

public class AddTagsClickListener implements View.OnClickListener {

    private final static String TAG = "AddTagsClickListener";

    private final Activity activity;
    private final Place event;

    public AddTagsClickListener(Activity activity, Place event) {
        this.activity = activity;
        this.event = event;
    }

    @Override
    public void onClick(View v) {
        // TODO fine location
        if (!MyApplication.hasLastLocation()) {
            Toast.makeText(activity, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
            return;
        }

        QueryCondition conditions = new QueryCondition();
        conditions.setPlaceId(event.remote_id);
        conditions.setAnonymous(false);
        conditions.setUserLocation(MyApplication.getLastLocation());
        Call<RestFeedback> call = RestClient.service().notifyPlaceHere(conditions.toMap());
        call.enqueue(new RestFeedbackCallback(activity) {
            @Override
            public void onActionSuccess(RestFeedback feedback) {
                Log.d(TAG, "Success register here for user");
            }

            @Override
            public void onActionFail(RestFeedback feedback) {
                Log.d(TAG, "Fail register here for user");
            }
        });

        IntentsUtils.addTags(activity, event);
    }
}
