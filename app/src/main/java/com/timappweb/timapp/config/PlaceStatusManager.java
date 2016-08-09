package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventStatus;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
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

    private class LastCallInfo{
        public HttpCallManager httpCallManager;
        public UserPlaceStatusEnum status;
        public long eventId;
    }
    private static LastCallInfo lastCallInfo;

    public static PlaceStatusManager instance(){
        if (_instance == null){
            _instance = new PlaceStatusManager();
        }
        return _instance;
    }


    private PlaceStatusManager() {
    }

    public HttpCallManager add(final Context context, final Event event, final UserPlaceStatusEnum status) {
        return add(context, event, status, 0L);
    }
    /**
     * @param context
     * @param event
     * @param status
     */
    public HttpCallManager add(final Context context, final Event event, final UserPlaceStatusEnum status, long callDelay) {

        if (this.isDuplicateRequest(event, status)){
            return null;
        }
        Log.d(TAG, "Initialize request to set status=" + status + " for event=" + event);

        Call<RestFeedback> call;
        // TODO call must be cancelable
        switch (status) {
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
                throw new UnsupportedOperationException();
        }

        if (lastCallInfo == null){
            lastCallInfo = new LastCallInfo();
        }
        lastCallInfo.eventId = event.getRemoteId();
        lastCallInfo.status = status;
        lastCallInfo.httpCallManager = RestClient.buildCall(call)
                .setCallDelay(callDelay)
                .onResponse(new HttpCallback<RestFeedback>() {
                    @Override
                    public void successful(RestFeedback feedback) {
                        Log.d(TAG, "Success register status=" + status + " for user on event: " + event);
                        EventStatus.setStatus(MyApplication.getCurrentUser(),
                                event,
                                status,
                                feedback.getIntData("id"));
                        QuotaManager.instance().add(QuotaType.NOTIFY_COMING);
                    }

                    @Override
                    public void failure() {
                        Log.e(TAG, "Fail register status=" + status + " for user on event: " + event);
                        Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
                    }
                })
                .perform();
        return lastCallInfo.httpCallManager;
    }
    /**
     *  @param context
     * @param event
     * @param status
     */
    public HttpCallManager cancel(final Context context, final Event event, final UserPlaceStatusEnum status) {
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
                throw new UnsupportedOperationException();
        }
        return RestClient.buildCall(call)
                .onResponse(new HttpCallback<RestFeedback>() {
                    @Override
                    public void successful(RestFeedback feedback) {
                        Log.d(TAG, "Success canceling status=" + status + " for user on event: " + event);
                        EventStatus.removeStatus(MyApplication.getCurrentUser(), event, status);
                    }

                    @Override
                    public void failure() {
                        Log.e(TAG, "Fail canceling status=" + status + " for user on event: " + event);
                        Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private QueryCondition _buildQuery(Event event){
        QueryCondition conditions = new QueryCondition();
        //conditions.setAnonymous(false);
        conditions.setUserLocation(LocationManager.getLastLocation());
        return conditions;
    }

    public HttpCallManager cancel(Context context, Event event) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return null;
        UserPlaceStatusEnum status = EventStatus.hasStatus(user.getId(), event.getId(), UserPlaceStatusEnum.COMING)
                ? UserPlaceStatusEnum.COMING
                : UserPlaceStatusEnum.HERE;
        return cancel(context, event, status);
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

    public boolean isDuplicateRequest(Event event, UserPlaceStatusEnum status) {
        EventStatus currentStatus = getStatus(event);

        // Cancel pending request if needed
        if (lastCallInfo != null){
            Log.d(TAG, "There are pending request '" + lastCallInfo.status+"' for event id '" + event.getRemoteId() + "'");
            if (!lastCallInfo.httpCallManager.getCall().isExecuted() && event.getRemoteId() == lastCallInfo.eventId){
                switch (status){
                    case COMING:
                    case HERE:
                        if (lastCallInfo.status == UserPlaceStatusEnum.GONE){
                            lastCallInfo.httpCallManager.cancel();
                            Log.i(TAG, "Cancelling request " +lastCallInfo.status+ " du to the opposite add " + status);
                            return true;
                        }
                        break;
                    case GONE:
                        if (lastCallInfo.status == UserPlaceStatusEnum.HERE || lastCallInfo.status == UserPlaceStatusEnum.COMING){
                            lastCallInfo.httpCallManager.cancel();
                            Log.i(TAG, "Cancelling request " +lastCallInfo.status+ " du to the opposite add " + status);
                            return true;
                        }
                }
            }
        }

        if (currentStatus != null && currentStatus.status == status){
            Log.d(TAG, "Trying to set the same status twice '"+status+"'... Skip task");
            return true;
        }

        return false;
    }



    public EventStatus addLocally(long syncId, Event event, UserPlaceStatusEnum status) {
        EventStatus eventStatus = new EventStatus();
        eventStatus.setRemoteId(syncId);
        eventStatus.status = status;
        eventStatus.user = MyApplication.getCurrentUser();
        eventStatus.created = (int)(System.currentTimeMillis()/1000);
        eventStatus.event = event;
        return (EventStatus) eventStatus.mySave();
    }

}
