package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;

/**
 * Created by Stephane on 09/08/2016.
 */
public class UserInvitationFeedback {

    @Expose
    public long                 user_id;
    @Expose
    public boolean              success;
    @Expose
    public boolean              notification;
    @Expose
    public EventsInvitation     invitation;


    @Override
    public String toString() {
        return "UserInvitationFeedback{" +
                "user_id=" + user_id +
                ", success=" + success +
                ", notification=" + notification +
                ", invitation=" + invitation +
                '}';
    }
}
