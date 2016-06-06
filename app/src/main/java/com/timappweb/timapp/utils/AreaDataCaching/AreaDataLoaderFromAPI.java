package com.timappweb.timapp.utils.AreaDataCaching;

import android.content.Context;
import android.util.Log;

import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.callbacks.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by stephane on 12/9/2015.
 */
public class AreaDataLoaderFromAPI implements AreaDataLoaderInterface<Event> {

    private static final String TAG = "AreaDataLoaderFromAPI";
    private Context mContext;
    private SearchFilter filter;
    private LoadingListener loadingListener;

    private ClusterManager<Event> mClusterManagerPlaces = null;
    private int requestCounter = 0;
    private int lastClear = -1;

    public void setAreaRequestHistory(AreaRequestHistory areaRequestHistory) {
        this.areaRequestHistory = areaRequestHistory;
    }

    private AreaRequestHistory areaRequestHistory = null;

    public void setLoadingListener(LoadingListener loadingListener) {
        this.loadingListener = loadingListener;
    }

    public AreaDataLoaderFromAPI(Context context, SearchFilter filter) {
        this.mContext = context;
        this.filter = filter;
    }



    public void setClusterManager(ClusterManager<Event> mClusterManagerPost) {
        Log.d(TAG, "setClusterManager");
        this.mClusterManagerPlaces = mClusterManagerPost;
    }

    public void clear(){
        areaRequestHistory.clearAll();
    }

    @Override
    public void load(final IntPoint pCpy, final AreaRequestItem request, QueryCondition conditions) {
        conditions.setTimeRange(ExploreMapFragment.getDataTimeRange());
        conditions.setMainTags(true);

        if (filter != null){
            conditions.setFilter(filter);
            Log.d(TAG, conditions.toString());
        }

        final int requestId = this.requestCounter++;

        Call<List<Event>> call = RestClient.service().bestPlaces(conditions.toMap());

        final int itemRequestId = request.setPendingCall(call);
        Log.i(TAG, "Request loading of area " + conditions.toString() + ". Request id: " + itemRequestId);

        if (loadingListener!=null) loadingListener.onLoadStart();
        call.enqueue(new RestCallback<List<Event>>() {

            @Override
            public void onFailure(Call call, Throwable t) {
                if (loadingListener!=null) loadingListener.onLoadEnd();
                super.onFailure(call, t);
            }

            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                super.onResponse(call, response);
                if (loadingListener!= null) loadingListener.onLoadEnd();

                if (request.isOutdated(itemRequestId)) {
                    Log.d(TAG, "Outdated request " + request.currentRequestId + " > " + itemRequestId + " . Do not load tags");
                    return;
                }

                if (response.isSuccessful()) {

                    if (requestId < lastClear) {
                        Log.d(TAG, "Outdated request " + requestId + " < " + lastClear + " . Do not load tags");
                        return;
                    }

                    List<Event> events = response.body();
                    Log.i(TAG, "WS loaded tags done. Loaded " + events.size() + " result(s). " + " for point " + pCpy);
                    // Setting the last timestamp retrieved from the server
                    // TODO what happens if two row have the same timestamp for the same area ?
                    request.setDataTimestamp(
                            events.size() > 1
                                    ? events.get(events.size() - 1).created
                                    : 0);
                    request.setData(events);
                }

            }
        });
    }

}
