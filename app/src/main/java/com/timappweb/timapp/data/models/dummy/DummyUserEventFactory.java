package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.UserEvent;

/**
 * Created by stephane on 5/25/2016.
 */
public class DummyUserEventFactory {

    public static UserEvent create(){
        UserEvent userEvent = new UserEvent();
        userEvent.id = 1L;

        userEvent.event = DummyEventFactory.create();
        userEvent.user = DummyUserFactory.create();
        userEvent.created = (System.currentTimeMillis());

        return userEvent;
    }

}
