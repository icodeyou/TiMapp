package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.config.AuthProviderInterface;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.WebServiceInterface;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public static final String ENDPOINT = "http://uat.timappweb.com/api/";

    public ApplicationTest() {
        super(Application.class);
    }

    @Before
    public static void initRestClient(){
        RestClient.init(new com.activeandroid.app.Application(), ENDPOINT, new DummyAuthProvider());
    }

    @Test
    public void testViewEvents() throws IOException {
        WebServiceInterface service = RestClient.service();
        RestQueryParams params = new RestQueryParams();
        // -73.56681, 45.46865
        params.setBounds(new LatLngBounds(new LatLng(40, 0), new LatLng(50, 10)));
        Call<List<Event>> call = service.bestPlaces(params.toMap());
        Response<List<Event>> response = call.execute();
        assertNotNull(response.body());
        List<Event> events = response.body();
    }

    private static class DummyAuthProvider implements AuthProviderInterface {
        @Override
        public String getToken() {
            return "fejiopzjpf2938020ezpjfpezoCEDEfez";
        }

        @Override
        public String getSocialProviderToken() {
            return "fejiopzjpf2938020ezpjfpezoCEDEfez";
        }

        @Override
        public void logout() {

        }

        @Override
        public HttpCallManager checkToken() {
            return null;
        }

        @Override
        public boolean login(User user, String token, String accessToken) {
            return false;
        }

        @Override
        public User getCurrentUser() {
            return new User();
        }

        @Override
        public boolean isLoggedIn() {
            return true;
        }
    }
}