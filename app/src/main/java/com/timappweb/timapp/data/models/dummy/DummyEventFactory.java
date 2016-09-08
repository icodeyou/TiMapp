package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.utils.Util;

import java.util.UUID;

/**
 * Created by stephane on 5/25/2016.
 */
public class DummyEventFactory {

    public static Event create(){
        Event event = new Event();
        event.remote_id = 1;
        event.count_here = 12;
        event.count_coming = 3;
        event.points = Util.getCurrentTimeSec() + 3600;
        event.loaded_time = event.points;
        event.name = uniqName();
        event.latitude = 12;
        event.longitude = 13;
        event.setSpot(DummySpotFactory.create());
        event.setCategory(new EventCategory("party"));
        event.description = "La fête de la musique c'est vraiment une occasion en or pour venir écouter une multitude de genre musicaux différents." +
                " C'est vraiment cool alors il ne faut pas rater ça!!";
        return event;
    }

    public static String uniqName() {
        return "Event " + UUID.randomUUID();
    }
}
