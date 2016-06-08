package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.User;

/**
 * Created by stephane on 6/8/2016.
 */
public class DummyUserFactory {

    public static User create(){
        User user = new User();
        user.setUsername("Stephane is here");
        user.remote_id = 1;
        return user;
    }
}
