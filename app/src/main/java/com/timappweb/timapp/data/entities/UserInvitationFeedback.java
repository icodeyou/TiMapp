package com.timappweb.timapp.data.entities;

import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;

/**
 * Created by Stephane on 09/08/2016.
 */
public class UserInvitationFeedback {

    public long                 user_id;
    public boolean              success;
    public boolean              notification;
    public EventsInvitation     invitation;

}
