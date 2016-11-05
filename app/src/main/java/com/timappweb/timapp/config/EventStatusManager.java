package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserEvent_Table;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.data.tables.BaseTable;
import com.timappweb.timapp.data.tables.EventsTable;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ClientError;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.KeyValueStorage;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;

/**
 * Created by stephane on 4/6/2016.
 */
public class EventStatusManager {

    private static final String KEY_CURRENT_EVENT = "current_event";
    private static final String TAG = "EventStatusManager";

    // ---------------------------------------------------------------------------------------------

    private static EventStatusManager _instance = null;
    private static Event currentEvent;
    //private static LastCallInfo lastCallInfo;

    public static boolean isCurrentEvent(long eventId) {
        return currentEvent != null && currentEvent.getRemoteId() == eventId;
    }

    public static void clearCurrentEvent() {
        SQLite.delete(UserEvent.class)
                .where(UserEvent_Table.event_id.eq(currentEvent.id))
                .and(UserEvent_Table.user_id.eq(MyApplication.getCurrentUser().id))
                .execute();
        currentEvent = null;
    }

    /*
    private class LastCallInfo{
        public HttpCallManager httpCallManager;
        public UserEventStatusEnum status;
        public long eventId;
    }*/

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
        //if (this.isDuplicateRequest(event, status)){
        //    return null;
        //}
        Log.d(TAG, "Initialize request to set status=" + status + " for event=" + event);

        Call<UserEvent> call;
        // TODO call must be cancelable
        switch (status) {
            case COMING:
                call = RestClient.service().notifyEventComing(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            case HERE:
                call = RestClient.service().notifyEventHere(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            case GONE:
                call = RestClient.service().cancelEventStatus(event.getRemoteId(), _buildQuery(event).toMap());
                break;
            default:
                Util.appStateError(TAG, "Trying to add an invalid status: " + status);
                return null;
        }
    /*
        if (lastCallInfo == null){
            lastCallInfo = new LastCallInfo();
        }
        lastCallInfo.eventId = event.getRemoteId();
        lastCallInfo.status = status;
        lastCallInfo.httpCallManager;*/
        return RestClient.buildCall(call)
                .setCallDelay(callDelay)
                .onResponse(new HttpCallback<UserEvent>() {
                    @Override
                    public void successful(UserEvent userEvent) {
                        if (userEvent == null){
                            Log.e(TAG, "Server returned a null response...");
                            // TODO remove existing status
                            EventStatusManager.removeLocally(event);
                            return;
                        }
                        try {
                            Log.d(TAG, "Success register status=" + status + " for user on event: " + event);
                            userEvent.event = event;
                            EventStatusManager.addLocally(userEvent);
                        } catch (CannotSaveModelException e) {
                            Log.e(TAG, "CannotSaveModelException: " + e.getMessage());
                        }
                    }

                    @Override
                    public void failure(ClientError clientError) {
                        if (this.response.code() != HttpsURLConnection.HTTP_UNAUTHORIZED) {
                            Log.e(TAG, "Fail register status=" + status + " for user on event: " + event);
                            Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .perform();
    }

    private static void removeLocally(Event event) {
        SQLite.delete(UserEvent.class)
                .where(UserEvent_Table.event_id.eq(event.id))
                .and(UserEvent_Table.user_id.eq(MyApplication.getCurrentUser().id))
                .execute();
    }

    private static UserEvent addLocally(UserEvent userEvent) throws CannotSaveModelException {
        userEvent.user = MyApplication.getCurrentUser();
        userEvent.created = Util.getCurrentTimeSec();
        userEvent.deepSave();
        if (userEvent.status == UserEventStatusEnum.HERE){
            EventStatusManager.setCurrentEvent(userEvent.event);
            SQLite.delete(UserEvent.class)
                    .where(UserEvent_Table.user_id.eq(userEvent.user.id))
                    .and(UserEvent_Table.status.eq(UserEventStatusEnum.HERE))
                    .and(UserEvent_Table.id.notEq(userEvent.id))
                    .execute();
        }
        // If we add the GONE status to our current event
        else if (userEvent.status == UserEventStatusEnum.GONE){
            if (currentEvent != null && currentEvent.equals(userEvent.event)){
                setCurrentEvent(null);
            }
        }
        QuotaManager.instance().add(QuotaType.NOTIFY_STATUS);
        return userEvent;
    }

    public static UserEvent addLocally(long syncId, Event event, UserEventStatusEnum status) throws CannotSaveModelException {
        UserEvent eventStatus = new UserEvent();
        eventStatus.id = syncId;
        eventStatus.status = status;
        eventStatus.event = event;
        return addLocally(eventStatus);
    }

    private RestQueryParams _buildQuery(Event event){
        RestQueryParams conditions = new RestQueryParams();
        //conditions.setAnonymous(false);
        conditions.setUserLocation(LocationManager.getLastLocation());
        return conditions;
    }

    private static UserEvent getStatus(Event event) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return null;

        return SQLite.select()
                .from(UserEvent.class)
                .where(UserEvent_Table.user_id.eq(user.id))
                .and(UserEvent_Table.event_id.eq(event.id))
                .orderBy(UserEvent_Table.id, false)
                .querySingle();
    }

    /*

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
                    public void failure(ClientError clientError) {
                        Log.e(TAG, "Fail canceling status=" + status + " for user on event: " + event);
                        Toast.makeText(context, context.getString(R.string.cannot_notify_status), Toast.LENGTH_SHORT).show();
                    }

                });
    }
    public HttpCallManager cancel(Context context, Event event) {
        User user = MyApplication.getCurrentUser();
        if (user == null) return null;
        UserEventStatusEnum status = UserEvent.hasStatus(user.getId(), event.getId(), UserEventStatusEnum.COMING)
                ? UserEventStatusEnum.COMING
                : UserEventStatusEnum.HERE;
        return cancel(context, event, status);
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
*/


    // ---------------------------------------------------------------------------------------------

    public static Event getCurrentEvent(){
        if (!MyApplication.isLoggedIn()){
            return null;
        }
        if (currentEvent != null){
            return currentEvent;
        }
        long eventId = KeyValueStorage.out().getLong(KEY_CURRENT_EVENT, -1);
        if (eventId == -1){
            return null;
        }
        Event event = EventsTable.load(eventId);

        if (event.isOver()){
            if (event.getLastSync() > event.last_activity){
                KeyValueStorage.in().remove(KEY_CURRENT_EVENT).commit();
                event = null;
            }
            else{
                BaseTable.requestSync(MyApplication.getApplicationBaseContext(), event);
                // TODO [critical] notify on sync end
            }
        }

        currentEvent = event;
        return currentEvent;
    }

    public static void setCurrentEvent(Event currentEvent) {
        EventStatusManager.currentEvent = currentEvent;
        if (currentEvent != null){
            KeyValueStorage.in().putLong(KEY_CURRENT_EVENT, currentEvent.getRemoteId()).commit();
        }
        else{
            KeyValueStorage.in().remove(KEY_CURRENT_EVENT).commit();
        }
    }

    public static boolean hasUserStatus(Event event, UserEventStatusEnum status) {
        if (status == UserEventStatusEnum.HERE){
            return KeyValueStorage.out().getLong(KEY_CURRENT_EVENT, -1) == event.getRemoteId();
        }
        else {
            UserEvent currentUserStatusInfo = getStatus(event);
            return currentUserStatusInfo != null && currentUserStatusInfo.event.getRemoteId() == event.getRemoteId();
        }
    }

    public static boolean isStatusUpToDate() {
        //TODO : Do. Please. Do. Stephane. Do it. Now. Move your ass and build me the awesomest method you've ever built.
        return true;
    }
}
