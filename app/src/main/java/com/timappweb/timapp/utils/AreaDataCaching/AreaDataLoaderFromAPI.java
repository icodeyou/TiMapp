package com.timappweb.timapp.utils.AreaDataCaching;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistoryBounds;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.IntPoint;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;

import retrofit2.Call;


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
    private AreaRequestHistory areaRequestHistory = null;

    private Event selectedEvent;

    // ---------------------------------------------------------------------------------------------

    public AreaDataLoaderFromAPI(Context context, SearchFilter filter) {
        this.mContext = context;
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
    public void load(final IntPoint pCpy, final AreaRequestItemInterface request, QueryCondition conditions) {
        conditions.setTimeRange(7200); // TODO cst
        conditions.setMainTags(true);
        if(selectedEvent!=null) {
            //TODO Steph : Add a condition if one event is selected : do not remove it from the map during the loading.
        };

        if (filter != null){
            conditions.setFilter(filter);
            Log.d(TAG, conditions.toString());
        }

        final int requestId = this.requestCounter++;

        Call<List<Event>> call = RestClient.service().bestPlaces(conditions.toMap());

        final int itemRequestId = request.setPendingCall(call);
        Log.i(TAG, "Request loading of area " + conditions.toString() + ". Request id: " + itemRequestId);

        if (loadingListener!=null) loadingListener.onLoadStart();

        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<Event>>() {
                    @Override
                    public void successful(List<Event> events) {
                        if (loadingListener!= null) loadingListener.onLoadEnd();
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
                                        ? events.get(events.size() - 1).created
                                        : 0);

                        request.setData(events);
                    }

                    @Override
                    public void notSuccessful() {
                        if (loadingListener!=null) loadingListener.onLoadEnd();
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
