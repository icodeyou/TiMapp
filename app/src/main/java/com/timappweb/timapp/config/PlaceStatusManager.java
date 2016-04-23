package com.timappweb.timapp.config;

import com.timappweb.timapp.data.models.PlaceStatus;
import com.timappweb.timapp.data.models.QuotaType;
import com.timappweb.timapp.entities.UserPlaceStatus;

/**
 * Created by stephane on 4/6/2016.
 */
public class PlaceStatusManager {

    /**
     * TODO
     * @param placeId
     * @param coming
     */
    public static void add(int placeId, UserPlaceStatus coming) {
        PlaceStatus.addStatus(placeId, coming);
        QuotaManager.instance().add(QuotaType.NOTIFY_COMING);
    }

}
