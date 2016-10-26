package com.timappweb.timapp.config.server;

/**
 * Created by Stephane on 15/10/2016.
 *
 * @warning Must be synchronized with server
 */
public class ServerNotifications {

    public static final String KEY_NOTIFICATION_TYPE = "notification_type";
    public static final String KEY_EVENT_ID = "event_id";
    public static final String KEY_USER_ID = "user_id";

    public static final String TYPE_OPEN_EVENT = "open_event";
    public static final String TYPE_REQUIRE_UPDATE = "require_update";
    public static final String TYPE_EVENT_INVITE = "event_invite";
}
