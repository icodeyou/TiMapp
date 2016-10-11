package com.timappweb.timapp.auth;

import java.util.EnumMap;

/**
 * Created by stephane on 2/18/2016.
 */
public enum SocialProvider{
    FACEBOOK("facebook");

    private final String name;

    SocialProvider(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
