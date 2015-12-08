package com.timappweb.timapp.utils.AreaDataCaching;

import android.content.Context;
import android.util.Log;

import com.google.maps.android.clustering.ClusterManager;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by stephane on 12/9/2015.
 */
public class AreaDataLoaderFromAPI implements AreaDataLoaderInterface {

    private static final String TAG = "AreaDataLoaderFromAPI";
    private Context mContext;
    private ClusterManager<MarkerValueInterface> mClusterManagerPost;

    public AreaDataLoaderFromAPI(Context context, ClusterManager<MarkerValueInterface> clusterManagerPost) {
        this.mContext = context;
        this.mClusterManagerPost = clusterManagerPost;
    }

    @Override
    public void load(final IntPoint pCpy, final AreaRequestItem request, QueryCondition conditions) {
        Log.i(TAG, "Request loading of area: " + conditions.toString());
        conditions.setVisualisation("post"); // For to return posts instead of cluster
        conditions.setTimeRange(3600);
        RestClient.service().listSpots(conditions.toMap(), new RestCallback<List<Post>>(mContext) {
            @Override
            public void success(List<Post> posts, Response response) {
                // If activity has been destroyed in the mean time
                // TODO cancel requests instead
                // if (mClusterManagerPost == null){
                //    return;
                //}

                Log.i(TAG, "WS loaded tags done. Loaded " + posts.size() + " result(s). " + " for point " + pCpy);
                //Toast.makeText(getActivity(), posts.size() + " tags loaded", Toast.LENGTH_LONG).show();
                request.data.addAll(posts);

                // TODO the server has to send back the timestamp
                if (posts.size() > 1)
                    request.setTimesamp(posts.get(posts.size() - 1).getCreated());
                else {
                    request.setTimesamp(0);
                }
                //mClusterManagerPost.clearItems();
                for (MarkerValueInterface d: posts){
                    mClusterManagerPost.addItem(d);
                }
                mClusterManagerPost.cluster();
            }
        });
    }
}
