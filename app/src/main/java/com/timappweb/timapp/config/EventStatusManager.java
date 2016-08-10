package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
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
public class EventStatusManager {

    private static final String TAG = "EventStatusManager";
    private static EventStatusManager _instance = null;
    private static Event currentEvent;



    private class LastCallInfo{
        public HttpCallManager httpCallManager;
        public UserEventStatusEnum status;
        public long eventId;
    }
    private static LastCallInfo lastCallInfo;

    public static EventStatusManager instance(){
        if (_instance == null){
            _instance = new EventStatusManager();
        }
        return _instance;
    }


    private EventStatusManager() {
    }

    public HttpCallManager add(final Context context, final Event event, final UserEventStatusEnum status) {
        return add(context, event, status, 0L);
    }
    /**
     * @param context
     * @param event
     * @param status
     */
    public HttpCallManager add(final Context context, final Event event, final UserEventStatusEnum status, long callDelay) {

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
                        UserEvent.setStatus(MyApplication.getCurrentUser(),
                                event,
                                status,
                                feedback.getIntData("id"));
                        QuotaManager.instance().add(QuotaType.NOTIFY_COMING);

                        if (status == UserEventStatusEnum.HERE){
                            EventStatusManager.setCurrentEvent(event);
                        }
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
    public HttpCallManager cancel(final Context context, final Event event, final UserEventStatusEnum status) {
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
                        UserEvent.removeStatus(MyApplication.getCurrentUser(), event, status);
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
        UserEventStatusEnum status = UserEvent.hasStatus(user.getId(), event.getId(), UserEventStatusEnum.COMING)
                ? UserEventStatusEnum.COMING
                : UserEventStatusEnum.HERE;
        return cancel(context, event, status);
    }

    public static UserEvent getStatus(Event event) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return null;
        return UserEvent.getStatus(event.getId(), user.getId());
    }

    public static boolean hasStatus(long placeId, UserEventStatusEnum status) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return false;
        return UserEvent.hasStatus(user.getId(), placeId, status);
    }

    public boolean isDuplicateRequest(Event event, UserEventStatusEnum status) {
        UserEvent currentStatus = getStatus(event);

        // Cancel pending request if needed
        if (lastCallInfo != null){
            Log.d(TAG, "There are pending request '" + lastCallInfo.status+"' for event id '" + event.getRemoteId() + "'");
            if (!lastCallInfo.httpCallManager.getCall().isExecuted() && event.getRemoteId() == lastCallInfo.eventId){
                switch (status){
                    case COMING:
                    case HERE:
                        if (lastCallInfo.status == UserEventStatusEnum.GONE){
                            lastCallInfo.httpCallManager.cancel();
                            Log.i(TAG, "Cancelling request " +lastCallInfo.status+ " du to the opposite add " + status);
                            return true;
                        }
                        break;
                    case GONE:
                        if (lastCallInfo.status == UserEventStatusEnum.HERE || lastCallInfo.status == UserEventStatusEnum.COMING){
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



    public UserEvent addLocally(long syncId, Event event, UserEventStatusEnum status) {
        UserEvent eventStatus = new UserEvent();
        eventStatus.setRemoteId(syncId);
        eventStatus.status = status;
        eventStatus.user = MyApplication.getCurrentUser();
        eventStatus.created = (int)(System.currentTimeMillis()/1000);
        eventStatus.event = event;
        return (UserEvent) eventStatus.mySave();
    }

    // ---------------------------------------------------------------------------------------------

    public static Event getCurrentEvent(){
        if (!MyApplication.isLoggedIn()){
            return null;
        }
        if (currentEvent != null){
            return currentEvent;
        }
        UserEvent lastHereStatus = new Select()
                .from(UserEvent.class)
                .where("Status = ? AND User = ?", UserEventStatusEnum.HERE, MyApplication.getCurrentUser())
                .orderBy("Created DESC")
                .executeSingle();
        if (lastHereStatus == null){
            return null;
        }
        Event event = lastHereStatus.event;
        // If event is hover but we didn't updated the data when it was done
        if (event.isOver() && (event.getLastSync() < event.getTimestampPoints()) ){
            event.requestSync();
        }
        else{
            Log.i(TAG, "Current event is now over...");
        }
        return event;
    }

    public static Event updateCurrentEventStatus(){
        currentEvent = null;
        return getCurrentEvent();
    }

    public static boolean hasCurrentEvent(){
        return getCurrentEvent() != null;
    }

    public static void setCurrentEvent(Event currentEvent) {
        EventStatusManager.currentEvent = currentEvent;
    }

}
