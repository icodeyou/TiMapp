package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void testViewEvents() throws IOException {
        RestQueryParams params = new RestQueryParams();
        // -73.56681, 45.46865
        params.setBounds(new LatLngBounds(new LatLng(40, 0), new LatLng(50, 10)));
        Call<List<Event>> call = RestClient.service().bestPlaces(params.toMap());
        Response<List<Event>> response = call.execute();
        assertNotNull(response.body());
        List<Event> events = response.body();
    }

}