package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.io.responses.EventPointResponse;

/**
 * Created by Stephane on 25/10/2016.
 */

public class UpdateEventCallback extends HttpCallback<EventPointResponse> {

    private Event event;

    public UpdateEventCallback(Event event) {
        this.event = event;
    }

    @Override
    public void successful(EventPointResponse eventPoint) {
        event.setPoints(eventPoint.points);
        event.setInactivityThreshold(eventPoint.inactivity_threshold);

        if (eventPoint.picture_id == 0){
            event.setBackgroundPicture(null);
        }
        else if (eventPoint.picture != null){
            event.setBackgroundPicture(eventPoint.picture);
            event.picture.event = event;
            event.picture = (Picture) event.picture.mySaveSafeCall();
        }
        event.mySaveSafeCall();
    }
}
