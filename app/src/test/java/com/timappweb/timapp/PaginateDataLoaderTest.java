package com.timappweb.timapp;

import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.DummyHttpRequestCall;

import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class PaginateDataLoaderTest{

    @Test
    public void testPaginateData() throws IOException, InterruptedException {
        final int limit = 5;
        final int total = 12;

        DummyPaginateProvider dataProvider = new DummyPaginateProvider(total, limit);
        PaginateDataLoader mDataLoader = new PaginateDataLoader<DummyItem>()
                .setDataProvider(dataProvider)
                .setCallback(new PaginateDataLoader.Callback() {
                    @Override
                    public void onLoadEnd(PaginateDataLoader.PaginateRequestInfo info, List data) {
                        assertNotNull(info);
                        assertNotNull(data);
                        assertEquals(data.size(), DummyPaginateProvider.getPageSize(total, info.getCurrentPage(), limit));
                        synchronized (PaginateDataLoaderTest.this){
                            PaginateDataLoaderTest.this.notify();
                        }
                    }

                    @Override
                    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
                        assertFalse("Should not trigger a load error", true);
                    }
                });

        synchronized (this){
            assertTrue(mDataLoader.hasNextPage());
            for (int i = 0; i < dataProvider.getTotalPage() - 1; i++){
                mDataLoader.loadNextPage();
                this.wait();
                assertTrue(mDataLoader.hasNextPage());
            }
            mDataLoader.loadNextPage();
            this.wait();
            assertFalse(mDataLoader.hasNextPage());
        }

    }

    /**
     *
     */
    private static class DummyPaginateProvider implements PaginateDataLoader.DataProvider {

        public int total;
        public int limit;

        public DummyPaginateProvider(int total, int limit) {
            this.total = total;
            this.limit = limit;
        }

        public int getTotalPage(){
            return total > 0 ? (total / limit) + 1 : 0;
        }

        @Override
        public HttpCallManager<PaginatedResponse> remoteLoad(final PaginateDataLoader.PaginateRequestInfo info) {
            return new HttpCallManager<>(new DummyHttpRequestCall<PaginatedResponse<DummyItem>>() {
                @Override
                public Response<PaginatedResponse<DummyItem>> execute() throws IOException {
                    if (info.getCurrentPage() > getTotalPage()){
                        return Response.error(404, new ResponseBody() {
                            @Override
                            public MediaType contentType() {
                                return null;
                            }

                            @Override
                            public long contentLength() {
                                return 0;
                            }

                            @Override
                            public BufferedSource source() {
                                return null;
                            }
                        });
                    }

                    int nbItemPage = getPageSize(total, info.getCurrentPage(), limit);
                    return Response.success(DummyPaginateProvider.generateResponse(nbItemPage >= 0 ? nbItemPage : 0, limit));
                }
            });
        }

        public static int getPageSize(int total, int page, int limit){
            return Math.max(0, Math.min(limit, total - ((page - 1) * limit)));
        }

        public static PaginatedResponse<DummyItem> generateResponse(int itemCount, int perPage){
            PaginatedResponse<DummyItem> response = new PaginatedResponse<DummyItem>();

            response.items = new LinkedList<DummyItem>();
            for (int i = 0; i < itemCount; i++){
                response.items.add(DummyItem.create());
            }
            response.perPage = perPage;
            response.extra = null;
            response.total = 0;

            return response;
        }

    }

    /**
     *
     */
    private static class DummyItem {
        public static int lastId = 0;

        public int id;
        public String name;

        public DummyItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static DummyItem create(){
            return new DummyItem(++lastId, UUID.randomUUID().toString());
        }
    }
}