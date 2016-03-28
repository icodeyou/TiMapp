package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 3/28/2016.
 */
public enum PlacesInvitationStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("accepted")
    ACCEPTED,
    @SerializedName("rejected")
    REJECTED
}
