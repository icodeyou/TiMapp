package com.timappweb.timapp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stephane on 05/09/2016.
 */
public class SectionDataLoaderTest {


    //private SectionDataLoader dataLoader;

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() throws IOException {

    }

    @Before
    public void setUp() {
        //DummyDataProvider.initData();
        // TODO rewrite
        /*
        dataLoader = new SectionDataLoader()
            .setDataProvider(new DummyDataProvider())
            .setOrder(SectionContainer.PaginateDirection.DESC)
            .setFormatter(new SectionDataLoader.SectionBoundsFormatter<Integer>() {
                @Override
                public long format(Integer i) {
                    return i;
                }
            });*/
    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void testLoadMore() throws InterruptedException {
        final List<Integer> loadedData = new LinkedList<>();
        /*
        dataLoader.setCallback(new SectionDataLoader.Callback<Integer>() {
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
                    synchronized (SectionDataLoaderTest.this) {
                        SectionDataLoaderTest.this.notify();
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
        }*/

        //assertEquals(DummyDataProvider.data, loadedData);
    }

    @Test
    public void testLoadNewest() throws InterruptedException {
        final int[] loadCount = {0};
        /*
        dataLoader.setCallback(new SectionDataLoader.Callback<Integer>() {
            @Override
            public void onLoadEnd(SectionContainer.PaginatedSection section, List<Integer> data) {
                loadCount[0]++;
                assertEquals( SectionContainer.LoadStatus.DONE, section.getStatus());
                switch (loadCount[0]){
                    case 1:
                    default:
                        assertEquals(DummyDataProvider.LIMIT, data.size());
                        assertFalse(dataLoader.loadNewest());
                        synchronized (SectionDataLoaderTest.this) {
                            SectionDataLoaderTest.this.notify();
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
        }*/

    }

    @Test
    public void testCache() throws InterruptedException {
        /*
        DummyCacheEngine cacheEngine = new DummyCacheEngine();
        dataLoader.setCallback(new SectionDataLoader.Callback() {
            @Override
            public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
                synchronized (SectionDataLoaderTest.this) {
                    SectionDataLoaderTest.this.notify();
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
        }*/
    }

    // ---------------------------------------------------------------------------------------------

    /*
    private static class DummyData{
        long id;
    }*/

    /*
    private static class DummyDataProvider implements SectionDataProviderInterface {

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
            HttpCallManager<ResponseSyncWrapper> callManager = new HttpCallManager<>(new DummyHttpRequestCall() {
                @Override
                public Response execute() throws IOException {
                    return Response.success(result);
                }
            });
            return callManager;
        }
    }

    private class DummyCacheEngine implements SectionDataLoader.CacheEngine<Integer> {

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
    }*/
}
