package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.timappweb.timapp.data.DBCacheEngine;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionDataProviderInterface;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.callbacks.PictureSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class PictureSectionDataLoaderTest extends ApplicationTestCase<Application> {

    public static final int EVENT_ID = 562;

    public PictureSectionDataLoaderTest() {
        super(Application.class);
    }

    @Test
    public void testPaginateEventPictures() throws IOException, InterruptedException {
        final Event event = DummyEventFactory.create();
        event.setRemoteId(EVENT_ID);

        long minDelayForceRefresh = 1000;
        final int remoteLoadLimit = 3;
        final List<Picture> loadedData = new LinkedList();

        SectionDataLoader mDataLoader = new SectionDataLoader<Picture>()
                .setFormatter(SyncBaseModel.getPaginatedFormater())
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayAutoRefresh(minDelayForceRefresh)
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
                .setDataProvider(new SectionDataProviderInterface() {

                    @Override
                    public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                        RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(remoteLoadLimit);
                        return RestClient.buildCall(RestClient.service().viewPicturesForPlace(event.getRemoteId(), options.toMap()));
                    }

                })
                .setCallback(new SectionDataLoader.Callback() {
                    @Override
                    public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
                        synchronized (PictureSectionDataLoaderTest.this){
                            loadedData.addAll(data);
                            PictureSectionDataLoaderTest.this.notify();
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
}