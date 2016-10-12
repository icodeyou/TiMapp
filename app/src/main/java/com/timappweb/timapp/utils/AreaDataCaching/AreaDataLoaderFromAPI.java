package com.timappweb.timapp.utils.AreaDataCaching;

import android.app.Activity;
import android.util.Log;

import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RetryOnErrorCallback;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by stephane on 12/9/2015.
 */
public class AreaDataLoaderFromAPI implements AreaDataLoaderInterface<Event> {

    private static final String TAG = "AreaDataLoaderFromAPI";
    private Activity mActivity;
    private SearchFilter filter;
    private LoadingListener loadingListener;

    private ClusterManager<Event> mClusterManagerPlaces = null;
    private int requestCounter = 0;
    private int lastClear = -1;
    private AreaRequestHistory areaRequestHistory = null;

    private Event selectedEvent;

    // ---------------------------------------------------------------------------------------------

    public AreaDataLoaderFromAPI(Activity activity, SearchFilter filter) {
        this.mActivity = activity;
        this.filter = filter;
    }


    public void setClusterManager(ClusterManager<Event> mClusterManagerPost) {
        Log.d(TAG, "setClusterManager");
        this.mClusterManagerPlaces = mClusterManagerPost;
    }

    public void clear(){
        if (areaRequestHistory != null) areaRequestHistory.clearAll();
    }

    @Override
    public void load(final IntPoint pCpy, final AreaRequestItemInterface request, RestQueryParams conditions) {
        conditions.setTimeRange(7200); // TODO cst
        conditions.setMainTags(false);

        if (filter != null){
            conditions.setFilter(filter);
            Log.d(TAG, conditions.toString());
        }

        final int requestId = this.requestCounter++;

        Call<List<Event>> call = RestClient.service().bestPlaces(conditions.toMap());

        final int itemRequestId = request.setPendingCall(call);
        Log.i(TAG, "Request loading of area " + conditions.toString() + ". Request id: " + itemRequestId);

        if (loadingListener!=null) loadingListener.onLoadStart();

        final HttpCallManager remoteCall = RestClient.buildCall(call);
        remoteCall
                .onResponse(new HttpCallback<List<Event>>() {
                    @Override
                    public void successful(List<Event> events) {
                        if (request.isOutdated(itemRequestId)) {
                            Log.d(TAG, "Outdated request " + request.getRequestId() + " > " + itemRequestId + " . Do not load tags");
                            return;
                        }
                        if (requestId < lastClear) {
                            Log.d(TAG, "Outdated request " + requestId + " < " + lastClear + " . Do not load tags");
                            return;
                        }
                        Log.i(TAG, "WS loaded tags done. Loaded " + events.size() + " result(s). " + " for point " + pCpy);
                        // Setting the last timestamp retrieved from the server
                        // TODO what happens if two row have the same timestamp for the same area ?
                        request.setDataTimestamp(
                                events.size() > 1
                                        ? events.get(events.size() - 1).getCreated()
                                        : 0);

                        request.setData(events);
                    }

                })
                .onError(new RetryOnErrorCallback(mActivity, new RetryOnErrorCallback.OnRetryCallback() {
                    @Override
                    public void onRetry() {
                        if (loadingListener != null) loadingListener.onLoadStart();
                        remoteCall.retry();
                    }
                }))
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        if (loadingListener != null) loadingListener.onLoadEnd();
                    }
                })
                .perform();
    }


    public void setAreaRequestHistory(AreaRequestHistory areaRequestHistory) {
        this.areaRequestHistory = areaRequestHistory;
    }

    public void setLoadingListener(LoadingListener loadingListener) {
        this.loadingListener = loadingListener;
    }

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        return;
    }


}
