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

    public PublishInEventCallback(Event event, User user, int actionType) {
        this.actionType = actionType;
        this.event = event;
    }

    public PublishInEventCallback(Event event, User user) {
        this(event, user, -1);
    }

    @Override
    public void successful(T feedback) {
        if (actionType != -1) QuotaManager.instance().add(actionType);
        RestClient.buildCall(RestClient.service().viewPointsPlace(event.getRemoteId()))
                .onResponse(new HttpCallback<EventPointResponse>() {
                    @Override
                    public void successful(EventPointResponse eventPoint) {
                        event.setPoints(eventPoint.points);
                    }
                })
                .perform();
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
