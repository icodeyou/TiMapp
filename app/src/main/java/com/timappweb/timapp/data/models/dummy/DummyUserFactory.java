package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 6/8/2016.
 */
public class DummyUserFactory {

    private static int createCounter = 0;

    public static User create(){
        User user = new User();
        user.setUsername("Bob " + createCounter);
        user.remote_id = createCounter+1;
        createCounter++;
        return user;
    }

    public static List<User> list(int nb) {
        LinkedList<User> list = new LinkedList();
        for (int i = 0; i < nb; i++){
            list.add(DummyUserFactory.create());
        }
        return list;
    }
}
