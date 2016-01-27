package com.timappweb.timapp.utils.AreaDataCaching;

import android.content.Context;
import android.util.Log;

import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by stephane on 12/9/2015.
 */
public class AreaDataLoaderFromAPI implements AreaDataLoaderInterface<Place> {

    private static final String TAG = "AreaDataLoaderFromAPI";
    private Context mContext;

    private ClusterManager<Place> mClusterManagerPost = null;
    private int requestCounter = 0;
    private int lastClear = -1;
    private PlacesAdapter placesAdapter = null;

    public AreaDataLoaderFromAPI(Context context) {
        this.mContext = context;
    }

    public void setClusterManager(ClusterManager<Place> mClusterManagerPost) {
        this.mClusterManagerPost = mClusterManagerPost;
    }

    public void setPlacesAdapter(PlacesAdapter placesAdapter) {
        this.placesAdapter = placesAdapter;
    }

    public AreaDataLoaderFromAPI(Context context, ClusterManager<Place> clusterManagerPost) {
        this.mContext = context;
        this.mClusterManagerPost = clusterManagerPost;
    }

    @Override
    public void load(final IntPoint pCpy, final AreaRequestItem request, QueryCondition conditions) {
        Log.i(TAG, "Request loading of area: " + conditions.toString());
        conditions.setTimeRange(ExploreMapFragment.getDataTimeRange());
        conditions.setMainTags(true);
        final int requestId = this.requestCounter++;
        RestClient.service().bestPlaces(conditions.toMap(), new RestCallback<List<Place>>(mContext) {
            @Override
            public void success(List<Place> places, Response response) {
                Log.i(TAG, "WS loaded tags done. Loaded " + places.size() + " result(s). " + " for point " + pCpy);
                // Test if request is out dated
                // TODO cancel requests instead
                if (requestId <= lastClear) {
                    Log.d(TAG, "This request is out dated");
                    return;
                }
                // If activity has been destroyed in the mean time
                // TODO cancel requests instead
                // if (mClusterManagerPost == null){
                //    return;
                //}

                // Setting the last timestamp retrieved from the server
                // TODO what happens if two row have the same timestamp for the same area ?
                request.setDataTimestamp(
                        places.size() > 1
                                ? places.get(places.size() - 1).created
                                : 0);

                request.data.addAll(places);

                if (mClusterManagerPost != null){
                    mClusterManagerPost.addItems(places);
                    mClusterManagerPost.cluster();
                }

                if (placesAdapter != null){
                    placesAdapter.clear();
                    placesAdapter.addAll(places);
                }
            }
        });
    }

    @Override
    public void clear(List<Place> data) {
        if (mClusterManagerPost != null){
            for (Place place: data){
                mClusterManagerPost.removeItem(place);
            }
        }
        if (placesAdapter != null){
            placesAdapter.clear();
        }
    }

    @Override
    public void clearAll() {
        lastClear = this.requestCounter;        // Every request id that is less or equal is out dated

        if (mClusterManagerPost != null){
            mClusterManagerPost.clearItems();
        }
        if (placesAdapter != null){
            placesAdapter.clear();
        }
    }
}
