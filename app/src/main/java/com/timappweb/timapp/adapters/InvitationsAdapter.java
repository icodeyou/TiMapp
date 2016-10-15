package com.timappweb.timapp.adapters;

import android.content.Context;

import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.data.models.EventsInvitation;

public class InvitationsAdapter extends MyFlexibleAdapter {
    private static final String TAG = "InvitationsAdapter";
    private Context context;

    public InvitationsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    public void add(EventsInvitation eventsInvitation) {
        addItem(getItemCount(), new InvitationItem(context,eventsInvitation));
    }
}