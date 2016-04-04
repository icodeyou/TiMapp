package com.timappweb.timapp.utils.AreaDataCaching;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.SearchFilter;
import com.timappweb.timapp.fragments.ExploreFragment;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by stephane on 12/9/2015.
 */
public class AreaDataLoaderFromAPI implements AreaDataLoaderInterface<Place> {

    private static final String TAG = "AreaDataLoaderFromAPI";
    private Context mContext;
    private SearchFilter filter;
    private LoadingListener loadingListener;

    private ClusterManager<Place> mClusterManagerPlaces = null;
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



    public void setClusterManager(ClusterManager<Place> mClusterManagerPost) {
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

        Call<List<Place>> call = RestClient.service().bestPlaces(conditions.toMap());

        final int itemRequestId = request.setPendingCall(call);
        Log.i(TAG, "Request loading of area " + conditions.toString() + ". Request id: " + itemRequestId);

        if (loadingListener!=null) loadingListener.onStart();
        call.enqueue(new RestCallback<List<Place>>(mContext) {

            @Override
            public void onFailure(Throwable t) {
                if (loadingListener!=null) loadingListener.onEnd();
                super.onFailure(t);
            }

            @Override
            public void onResponse(Response<List<Place>> response) {
                super.onResponse(response);
                if (loadingListener!= null) loadingListener.onEnd();

                if (request.isOutdated(itemRequestId)) {
                    Log.d(TAG, "Outdated request " + request.currentRequestId + " > " + itemRequestId + " . Do not load tags");
                    return;
                }

                if (response.isSuccess()) {

                    if (requestId < lastClear) {
                        Log.d(TAG, "Outdated request " + requestId + " < " + lastClear + " . Do not load tags");
                        return;
                    }

                    List<Place> places = response.body();
                    Log.i(TAG, "WS loaded tags done. Loaded " + places.size() + " result(s). " + " for point " + pCpy);
                    // Setting the last timestamp retrieved from the server
                    // TODO what happens if two row have the same timestamp for the same area ?
                    request.setDataTimestamp(
                            places.size() > 1
                                    ? places.get(places.size() - 1).created
                                    : 0);
                    request.setData(places);
                }

            }
        });
    }

}
