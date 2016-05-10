package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.PlaceStatus;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.model.RestFeedback;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 4/6/2016.
 */
public class PlaceStatusManager {

    private static final String TAG = "PlaceStatusManager";
    private static PlaceStatusManager _instance = null;

    //private OnStatusUpdateCallback onChangeStatusCallback = null;

    public static PlaceStatusManager instance(){
        if (_instance == null){
            _instance = new PlaceStatusManager();
        }
        return _instance;
    }


    private PlaceStatusManager() {
        //this.onChangeStatusCallback = onChangeStatusCallback;
    }

    /**
     *
     * @param context
     * @param place
     * @param status
     */
    public void add(Context context, Place place, UserPlaceStatusEnum status) {
        _addOnRemote(context, place, status);

    }
    /**
     *
     * @param context
     * @param place
     * @param status
     */
    public void cancel(Context context, Place place, UserPlaceStatusEnum status) {
        _removeOnRemote(context, place, status);
    }

    private QueryCondition _buildQuery(Place place){
        QueryCondition conditions = new QueryCondition();
        conditions.setPlaceId((int) place.getRemoteId());
        //conditions.setAnonymous(false);
        conditions.setUserLocation(MyApplication.getLastLocation());
        return conditions;
    }

    private void _removeOnRemote(Context context, Place place, final UserPlaceStatusEnum status){

        Call<RestFeedback> call;
        // TODO call must be cancelable
        switch (status){
            case COMING:
                call = RestClient.service().cancelComing(_buildQuery(place).toMap());
                break;
            case HERE:
                call = RestClient.service().cancelHere(_buildQuery(place).toMap());
                break;
            default:
                Log.v(TAG, "Nothing to do on remote for status: " + status);
                return;
        }
        call.enqueue(new OnStatusUpdateCallback(context, place, status));
    }

    public void _addOnRemote(Context context, Place place, final UserPlaceStatusEnum status){
        Call<RestFeedback> call;
        // TODO call must be cancelable
        switch (status){
            case COMING:
                call = RestClient.service().notifyPlaceComing(_buildQuery(place).toMap());
                break;
            case HERE:
                call = RestClient.service().notifyPlaceHere(_buildQuery(place).toMap());
                break;
            case GONE:
                call = RestClient.service().notifyPlaceGone(_buildQuery(place).toMap());
                break;
            default:
                Log.v(TAG, "Nothing to do on remote for status: " + status);
                return;
        }
        call.enqueue(new OnStatusUpdateCallback(context, place, status));
    }

    public void cancel(Context context, Place place) {
        UserPlaceStatusEnum status = PlaceStatus.hasStatus(place.getId(), UserPlaceStatusEnum.COMING)
                ? UserPlaceStatusEnum.COMING
                : UserPlaceStatusEnum.HERE;
        cancel(context, place, status);
    }


    public class OnStatusUpdateCallback extends RestFeedbackCallback {

        private final Place place;
        private final UserPlaceStatusEnum status;

        public OnStatusUpdateCallback(Context context, Place place, UserPlaceStatusEnum status) {
            super(context);
            this.place = place;
            this.status = status;
        }


        @Override
        public void onActionSuccess(RestFeedback feedback) {
            Log.d(TAG, "Success register status=" +status+ " for user on event: " + place);
            PlaceStatus.addStatus(place, status);
            QuotaManager.instance().add(QuotaType.NOTIFY_COMING);
        }

        @Override
        public void onActionFail(RestFeedback feedback) {
            Log.e(TAG, "Fail register status="+status + " for user on event: " + place);
            Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
        }

    }
}
