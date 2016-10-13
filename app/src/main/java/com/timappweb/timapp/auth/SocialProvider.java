package com.timappweb.timapp.auth;

/**
 * Created by stephane on 2/18/2016.
 *
 * MUST BE SYNC WITH SERVER
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

    public static SocialProvider fromString(String text) {
        if (text != null) {
            for (SocialProvider b : SocialProvider.values()) {
                if (text.equalsIgnoreCase(b.name)) {
                    return b;
                }
            }
        }
        return null;
    }
}
