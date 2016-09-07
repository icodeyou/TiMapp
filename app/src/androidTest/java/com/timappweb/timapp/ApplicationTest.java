package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.config.AuthProviderInterface;
import com.timappweb.timapp.data.DBCacheEngine;
import com.timappweb.timapp.data.loader.PaginatedDataLoader;
import com.timappweb.timapp.data.loader.PaginatedDataProviderInterface;
import com.timappweb.timapp.data.loader.SectionContainer;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.services.WebServiceInterface;
import com.timappweb.timapp.sync.callbacks.PictureSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public static final int EVENT_ID = 562;

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


    @Test
    public void testPaginateEventPictures() throws IOException, InterruptedException {
        final Event event = DummyEventFactory.create();
        event.setRemoteId(EVENT_ID);

        long minDelayForceRefresh = 1000;
        final int remoteLoadLimit = 3;
        final List<Picture> loadedData = new LinkedList();

        PaginatedDataLoader mDataLoader = new PaginatedDataLoader<Picture>()
                .setFormatter(SyncBaseModel.getPaginatedFormater())
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayRefresh(minDelayForceRefresh)
                .setCacheEngine(new DBCacheEngine<Picture>(Picture.class) {
                    @Override
                    protected String getHashKey() {
                        return "TmpEventPicture" + event.getRemoteId();
                    }

                    @Override
                    protected void persist(List<Picture> data) throws Exception {
                        new MultipleEntriesSyncPerformer<Picture, ResponseSyncWrapper<Picture>>()
                                .setRemoteEntries(data)
                                .setLocalEntries(event.getPictures())
                                .setCallback(new PictureSyncCallback(event))
                                .perform();
                    }
                })
                .setDataProvider(new PaginatedDataProviderInterface() {

                    @Override
                    public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                        RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(remoteLoadLimit);
                        return RestClient.buildCall(RestClient.service().viewPicturesForPlace(event.getRemoteId(), options.toMap()));
                    }

                })
                .setCallback(new PaginatedDataLoader.Callback() {
                    @Override
                    public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
                        synchronized (ApplicationTest.this){
                            loadedData.addAll(data);
                            ApplicationTest.this.notify();
                        }
                    }

                    @Override
                    public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
                        assertFalse("API should not retrun a error: " + error.getMessage(), true);
                    }
                });

        synchronized (this){
            mDataLoader.loadNewest();
            this.wait();
            assertEquals(remoteLoadLimit, loadedData.size());

            mDataLoader.loadMore();
            this.wait();
            assertEquals(remoteLoadLimit, loadedData.size());
        }

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