package com.timappweb.timapp.utils;

import com.timappweb.timapp.data.loader.PaginatedDataLoader;
import com.timappweb.timapp.data.loader.PaginatedDataProviderInterface;
import com.timappweb.timapp.data.loader.SectionContainer;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;

/**
 * Created by Stephane on 05/09/2016.
 */
public class PaginatedDataLoaderTest {


    private PaginatedDataLoader dataLoader;

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() throws IOException {

    }

    @Before
    public void setUp() {
        DummyDataProvider.initData();
        dataLoader = new PaginatedDataLoader()
            .setDataProvider(new DummyDataProvider())
            .setOrder(SectionContainer.PaginateDirection.DESC)
            .setFormatter(new PaginatedDataLoader.SectionBoundsFormatter<Integer>() {
                @Override
                public long format(Integer i) {
                    return i;
                }
            });
    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void testLoadMore() throws InterruptedException {
        final List<Integer> loadedData = new LinkedList<>();
        dataLoader.setCallback(new PaginatedDataLoader.Callback<Integer>() {
            @Override
            public void onLoadEnd(SectionContainer.PaginatedSection section, List<Integer> data) {
                assertEquals(Math.min(DummyDataProvider.data.size() - loadedData.size(), DummyDataProvider.LIMIT), data.size());
                assertEquals( SectionContainer.LoadStatus.DONE, section.getStatus());
                assertNotSame(-1, section.end);
                assertNotSame(-1, section.start);
                loadedData.addAll(data);
                if (!dataLoader.isFullyLoaded()){
                    dataLoader.loadMore();
                }
                else {
                    synchronized (PaginatedDataLoaderTest.this) {
                        PaginatedDataLoaderTest.this.notify();
                    }
                }
            }

            @Override
            public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
                assertFalse("Should not trigger a load error", true);
            }
        });
        synchronized (this) {
            dataLoader.loadMore();
            while (DummyDataProvider.data.size() - loadedData.size() > 0) this.wait();
        }

        assertEquals(DummyDataProvider.data, loadedData);
    }

    @Test
    public void testLoadNewest() throws InterruptedException {
        final int[] loadCount = {0};
        dataLoader.setCallback(new PaginatedDataLoader.Callback<Integer>() {
            @Override
            public void onLoadEnd(SectionContainer.PaginatedSection section, List<Integer> data) {
                loadCount[0]++;
                assertEquals( SectionContainer.LoadStatus.DONE, section.getStatus());
                switch (loadCount[0]){
                    case 1:
                    default:
                        assertEquals(DummyDataProvider.LIMIT, data.size());
                        assertFalse(dataLoader.loadNewest());
                        synchronized (PaginatedDataLoaderTest.this) {
                            PaginatedDataLoaderTest.this.notify();
                        }
                        break;
                }
            }

            @Override
            public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
                assertFalse("Should not trigger a load error", true);
            }
        });

        synchronized (this) {
            dataLoader.getSectionContainer().addSection(new SectionContainer.PaginatedSection(10, 17).setStatus(SectionContainer.LoadStatus.DONE));
            dataLoader.loadNewest();
            while (loadCount[0] < 1) this.wait();
        }

    }

    @Test
    public void testCache() throws InterruptedException {
        DummyCacheEngine cacheEngine = new DummyCacheEngine();
        dataLoader.setCallback(new PaginatedDataLoader.Callback() {
            @Override
            public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
                synchronized (PaginatedDataLoaderTest.this) {
                    PaginatedDataLoaderTest.this.notify();
                }
            }

            @Override
            public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {

            }
        })
                .setCacheEngine(cacheEngine);
        synchronized (this) {
            dataLoader.loadMore();
            this.wait();
            assertEquals(0, cacheEngine.getCacheLoadCount());
            dataLoader.getSectionContainer().clear();
            // Should load from cache
            dataLoader.loadMore();
            assertEquals(1, cacheEngine.getCacheLoadCount());
        }
    }

    // ---------------------------------------------------------------------------------------------

    /*
    private static class DummyData{
        long id;
    }*/

    private static class DummyDataProvider implements PaginatedDataProviderInterface {

        public static final int DATA_COUNT = 32;
        public static final int LIMIT = 6;

        private static List<Integer> data = new LinkedList<>();

        public static void initData(){
            for (int i = 0; i < DATA_COUNT; i++){
                data.add(i);
            }
        }
        public static List<Integer> buildDataSection(List<Integer> data, SectionContainer.PaginatedSection section){
            int start = section.start == -1 ? 0 : (int) section.start;
            int end = section.end == -1
                    ? start + LIMIT
                    : (int) (section.end + 1);
            end = Math.min(data.size(), end);

            if (Math.abs(end - start) + 1 > LIMIT){
                end = start + LIMIT;
            }
            return data.subList(start, end);
        }

        @Override
        public HttpCallManager<ResponseSyncWrapper> remoteLoad(SectionContainer.PaginatedSection section) {
            final ResponseSyncWrapper result = new ResponseSyncWrapper<Integer>();
            result.items = buildDataSection(data, section);
            result.last_update = System.currentTimeMillis();
            result.up_to_date = result.items.size() < LIMIT;
            result.limit = LIMIT;
            HttpCallManager<ResponseSyncWrapper> callManager = new HttpCallManager<>(new Call<ResponseSyncWrapper>() {
                @Override
                public Response<ResponseSyncWrapper> execute() throws IOException {
                    return Response.success(result);
                }

                @Override
                public void enqueue(Callback<ResponseSyncWrapper> callback) {
                    try {
                        callback.onResponse(this, this.execute());
                    } catch (IOException e) {
                        callback.onFailure(this, e);
                    }
                }

                @Override
                public boolean isExecuted() {
                    return true;
                }

                @Override
                public void cancel() {

                }

                @Override
                public boolean isCanceled() {
                    return false;
                }

                @Override
                public Call<ResponseSyncWrapper> clone() {
                    return null;
                }

                @Override
                public Request request() {
                    return null;
                }
            });
            return callManager;
        }
    }

    private class DummyCacheEngine implements PaginatedDataLoader.CacheEngine<Integer> {

        List<Integer> data;
        SectionContainer sections;
        int cacheLoadCount;

        public DummyCacheEngine() {
            this.sections = new SectionContainer();
            this.data = new LinkedList<>();
            this.cacheLoadCount = 0;
        }

        @Override
        public void add(SectionContainer.PaginatedSection<Integer> section, List<Integer> data) {
            this.data.addAll(data);
            sections.addSection(section);
        }

        @Override
        public boolean contains(SectionContainer.PaginatedSection<Integer> section) {
            return sections.isLoaded(section);
        }

        @Override
        public List<Integer> get(SectionContainer.PaginatedSection<Integer> section) {
            cacheLoadCount++;
            return DummyDataProvider.buildDataSection(data, section);
        }

        public int getCacheLoadCount() {
            return cacheLoadCount;
        }
    }
}
