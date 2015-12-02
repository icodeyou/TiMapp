package com.timappweb.timapp.utils;

import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.RestFeedback;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.WebServiceInterface;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by stephane on 9/17/2015.
 */
public class RestApiTest {

    @Test
    public void testViewPost(){
        WebServiceInterface service = RestClient.service();
        Post post = service.viewSpot(1);
        assertNotEquals(post, null);

    }

    public void testLogin(){
        User user = new User();
        user.username = "dummy@tiemapp.com";
        user.password = "dummy";
        RestFeedback feedback = RestClient.service().login(user);
        assertNotEquals(feedback, null);
        assert(feedback.returnCode == 0);
    }


}