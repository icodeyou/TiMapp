package com.timappweb.timapp.data.loader.paginate;

import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import java.util.List;

import retrofit2.Response;

/**
 * Created by Stephane on 09/09/2016.
 */
public class PaginateDataLoader<T> {

    Callback callback;
    DataProvider dataProvider;
    PaginateRequestInfo pageInfo;
    private HttpCallManager remoteCall;

    // ---------------------------------------------------------------------------------------------

    public PaginateDataLoader() {
        this.clear();
    }

    // ---------------------------------------------------------------------------------------------


    public boolean hasNextPage() {
        return pageInfo.hasNextPage();
    }

    public boolean loadNextPage(){
        if (!pageInfo.hasNextPage() || isLoading()){
            return false;
        }
        pageInfo.nextPage();
        return loadCurrentPage();
    }

    public boolean loadCurrentPage(){
        if (isInCache(pageInfo.getCurrentPage())){
            List items = getCache(pageInfo.getCurrentPage());
            if (callback != null) callback.onLoadEnd(pageInfo, items);
        }
        else{
            remoteCall = this.dataProvider.remoteLoad(pageInfo)
                    .onResponse(new HttpCallback<PaginatedResponse<T>>() {
                        @Override
                        public void successful(PaginatedResponse<T> feedback) {
                            if (feedback.isLastPage()){
                                pageInfo.setLastPage();
                            }
                            if (callback != null) callback.onLoadEnd(pageInfo, feedback.items);
                        }

                        @Override
                        public void notSuccessful() {
                            if (callback != null) callback.onLoadError(new Exception("Unexpected server response"), pageInfo);
                        }
                    })
                    .onError(new RequestFailureCallback(){
                        @Override
                        public void onError(Throwable error) {
                            if (callback != null) callback.onLoadError(error, pageInfo);
                        }
                    })
                    .onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {

                        }
                    })
                    .perform();
        }
        return false;
    }

    private List getCache(int page) {
        return null;
    }

    private boolean isInCache(int page) {
        return false;
    }

    public boolean isLoading(){
        return remoteCall != null && !remoteCall.isDone();
    }

    public PaginateDataLoader<T> clear(){
        this.pageInfo = new PaginateRequestInfo();
        return this;
    }
    // ---------------------------------------------------------------------------------------------


    public Callback getCallback() {
        return callback;
    }

    public PaginateDataLoader setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public PaginateDataLoader setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     *
     * @param <T>
     */
    public interface DataProvider<T> {

        HttpCallManager<PaginatedResponse<T>> remoteLoad(PaginateRequestInfo info);

    }


    public static class PaginateRequestInfo{

        private RestQueryParams params;
        private int lastPage;

        public PaginateRequestInfo() {
            this.clear();
        }

        public int getCurrentPage(){
            return params.getInt(RestQueryParams.SYNC_PARAM_PAGE);
        }

        public boolean hasNextPage(){
            return lastPage == -1 || this.getCurrentPage() < lastPage;
        }

        public void clear(){
            this.lastPage = -1;
            this.params = new RestQueryParams()
                .setPage(0);
        }

        public void nextPage(){
            params.setPage(this.getCurrentPage() + 1);
        }

        public void setLastPage(){
            this.lastPage = this.getCurrentPage();
        }

        public RestQueryParams getQueryParams(){
            return params;
        }
    }

    /**
     *
     * @param <T>
     */
    public interface Callback<T>{

        void onLoadEnd(PaginateRequestInfo info, List<T> data);

        void onLoadError(Throwable error, PaginateRequestInfo info);

    }

}
