package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.responses.EventPointResponse;

/**
 * Created by stephane on 6/7/2016.
 */
public class PublishInEventCallback<T> extends HttpCallback<T> {

    private final int actionType;
    private final Event event;
    private final boolean udateEvent;

    public PublishInEventCallback(Event event, User user, int actionType) {
        this(event, user, actionType, true);
    }

    public PublishInEventCallback(Event event, User user) {
        this(event, user, -1);
    }

    public PublishInEventCallback(Event event, User user, int actionType, boolean b) {
        this.actionType = actionType;
        this.event = event;
        this.udateEvent = b;
    }

    @Override
    public void successful(T feedback) {
        if (actionType != -1) QuotaManager.instance().add(actionType);
        if (udateEvent){
            RestClient.buildCall(RestClient.service().viewPointsPlace(event.getRemoteId()))
                    .onResponse(new HttpCallback<EventPointResponse>() {
                        @Override
                        public void successful(EventPointResponse eventPoint) {
                            event.setPoints(eventPoint.points);
                            event.setInactivityThreshold(eventPoint.inactivity_threshold);
                        }
                    })
                    .perform();
        }
        if (actionType == QuotaType.ADD_PICTURE || actionType == QuotaType.ADD_TAGS){
            if (EventStatusManager.hasCurrentEvent() && !EventStatusManager.getCurrentEvent().equals(event)){
                // TODO add here status permanentely (we need the sync id from the server...)
                EventStatusManager.setCurrentEvent(event);
            }

        }
    }


    @Override
    public void forbidden() {
        // TODO set quota..
    }
}
