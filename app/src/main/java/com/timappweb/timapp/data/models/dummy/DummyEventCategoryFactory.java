package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.EventCategory;

/**
 * Created by Stephane on 27/09/2016.
 */
public class DummyEventCategoryFactory {

    public static EventCategory create() {
        EventCategory eventCategory = new EventCategory("party");
        eventCategory.setRemoteId(1);
        return eventCategory;
    }

}
