package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventStatus;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.location.LocationManager;

import retrofit2.Call;

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
     *  @param context
     * @param event
     * @param status
     * @param listener
     */
    public void add(Context context, Event event, UserPlaceStatusEnum status, BinaryActionListener listener) {
        _addOnRemote(context, event, status, listener);

    }
    /**
     *
     * @param context
     * @param event
     * @param status
     */
    public void cancel(Context context, Event event, UserPlaceStatusEnum status, BinaryActionListener listener) {
        _removeOnRemote(context, event, status, listener);
    }

    private QueryCondition _buildQuery(Event event){
        QueryCondition conditions = new QueryCondition();
        //conditions.setAnonymous(false);
        conditions.setUserLocation(LocationManager.getLastLocation());
        return conditions;
    }

    private void _removeOnRemote(Context context, Event event, final UserPlaceStatusEnum status, BinaryActionListener listener){

        Call<RestFeedback> call;
        // TODO call must be cancelable
        switch (status){
            case COMING:
                call = RestClient.service().cancelComing(event.getRemoteId());
                break;
            case HERE:
                call = RestClient.service().cancelHere(event.getRemoteId());
                break;
            default:
                Log.v(TAG, "Nothing to do on remote for status: " + status);
                return;
        }
        call.enqueue(new OnStatusCancelCallback(context, event, status, listener));
    }

    public void _addOnRemote(Context context, Event event, final UserPlaceStatusEnum status, BinaryActionListener listener){
        Call<RestFeedback> call;
        // TODO call must be cancelable
        switch (status){
            case COMING:
                call = RestClient.service().notifyPlaceComing(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            case HERE:
                call = RestClient.service().notifyPlaceHere(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            case GONE:
                call = RestClient.service().notifyPlaceGone(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            default:
                Log.v(TAG, "Nothing to do on remote for status: " + status);
                return;
        }
        call.enqueue(new OnStatusAddCallback(context, event, status, listener));
    }

    public void cancel(Context context, Event event, BinaryActionListener listener) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return;
        UserPlaceStatusEnum status = EventStatus.hasStatus(user.getId(), event.getId(), UserPlaceStatusEnum.COMING)
                ? UserPlaceStatusEnum.COMING
                : UserPlaceStatusEnum.HERE;
        cancel(context, event, status, listener);
    }

    public static EventStatus getStatus(Event event) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return null;
        return EventStatus.getStatus(event.getId(), user.getId());
    }

    public static boolean hasStatus(long placeId, UserPlaceStatusEnum status) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return false;
        return EventStatus.hasStatus(user.getId(), placeId, status);
    }


    public class OnStatusAddCallback extends RestFeedbackCallback {

        private final Event event;
        private final UserPlaceStatusEnum status;
        private final BinaryActionListener listener;

        public OnStatusAddCallback(Context context, Event event, UserPlaceStatusEnum status, BinaryActionListener listener) {
            super(context);
            this.event = event;
            this.status = status;
            this.listener = listener;
        }


        @Override
        public void onActionSuccess(RestFeedback feedback) {
            Log.d(TAG, "Success register status=" +status+ " for user on event: " + event);
            EventStatus.setStatus(MyApplication.getCurrentUser(), event, status, feedback.getIntData("id"));
            QuotaManager.instance().add(QuotaType.NOTIFY_COMING);
            listener.onSuccess();
        }

        @Override
        public void onActionFail(RestFeedback feedback) {
            Log.e(TAG, "Fail register status="+status + " for user on event: " + event);
            Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
            listener.onFailure();
        }

        @Override
        public void onFinish() {
            listener.onFinish();
        }
    }


    public class OnStatusCancelCallback extends RestFeedbackCallback {

        private final Event event;
        private final UserPlaceStatusEnum status;
        private final BinaryActionListener listener;

        public OnStatusCancelCallback(Context context, Event event, UserPlaceStatusEnum status, BinaryActionListener listener) {
            super(context);
            this.event = event;
            this.status = status;
            this.listener = listener;
        }


        @Override
        public void onActionSuccess(RestFeedback feedback) {
            Log.d(TAG, "Success canceling status=" + status + " for user on event: " + event);
            EventStatus.removeStatus(MyApplication.getCurrentUser(), event, status);
            listener.onSuccess();
        }

        @Override
        public void onActionFail(RestFeedback feedback) {
            Log.e(TAG, "Fail canceling status=" + status + " for user on event: " + event);
            Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
            listener.onFailure();
        }

        @Override
        public void onFinish() {
            listener.onFinish();
        }
    }
}
