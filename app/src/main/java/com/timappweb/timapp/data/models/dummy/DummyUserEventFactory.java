package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 5/25/2016.
 */
public class DummyUserEventFactory {

    public static UserEvent create(){
        UserEvent userEvent = new UserEvent();
        userEvent.remote_id = 1;

        userEvent.event = DummyEventFactory.create();
        userEvent.user = DummyUserFactory.create();
        userEvent.created = (int) (System.currentTimeMillis() * 1000);

        return userEvent;
    }

}
