package com.timappweb.timapp.rest.io.responses;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.Picture;

/**
 * Created by Stephane on 14/09/2016.
 */
public class EventPointResponse {

    @Expose
    public int points;

    @Expose
    public int inactivity_threshold;

    @Expose
    public Picture picture;

    @Expose
    public int picture_id = -1;

}
