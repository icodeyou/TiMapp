package com.timappweb.timapp.utils;

import com.timappweb.timapp.R;

/**
 * Created by Stephane on 11/08/2016.
 */
public class DrawableUtil {

    public static int get(String name) throws UnknownDrawableException {
        try {
            return R.drawable.class.getField(name).getInt(null);
        } catch (Exception e) {
            throw new UnknownDrawableException("Cannot find drawable '" + name + "'");
        }
    }

    public static class UnknownDrawableException extends Throwable {

        public UnknownDrawableException(String msg) {
            super(msg);
        }
    }
}
