package com.timappweb.timapp.listeners;

import android.content.Context;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;

public class FabListenerFactory {

    public static final String TAG = "FabListenerFactory";

    public FabListenerFactory() {

    }

    public static void setFabListener(final Context context, View root, final Event event) {
        View cameraButton = root.findViewById(R.id.action_camera);
        View tagButton = root.findViewById(R.id.action_tag);
        View inviteButton = root.findViewById(R.id.action_invite);
        View comingButton = root.findViewById(R.id.action_coming);

        if(cameraButton != null) {
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_CAMERA);
                }
            });
        }

        if(tagButton != null) {
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_TAGS);
                }
            });
        }

        if(inviteButton != null) {
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_PEOPLE);
                }
            });
        }

        if(comingButton != null) {
            comingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_COMING);
                }
            });
        }
    }
}
