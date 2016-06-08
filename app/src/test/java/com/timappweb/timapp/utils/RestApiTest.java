package com.timappweb.timapp.utils;


import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.services.WebServiceInterface;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by stephane on 9/17/2015.
 * TODO complete tests
 */
public class RestApiTest {

    @Test
    public void testViewPost() throws IOException {
        WebServiceInterface service = RestClient.service();
        retrofit2.Call<EventPost> call = service.viewPost(1);
        Response<EventPost> response = call.execute();
        assertNotEquals(response.body(), null);

    }

    /*
    public void testLogin(){
        User user = new User();
        user.username = "dummy@tiemapp.com";
        user.password = "dummy";


        RestFeedback feedback = RestClient.service().login(user);
        assertNotEquals(feedback, null);
        assert(feedback.code == 0);
    }
    */


}