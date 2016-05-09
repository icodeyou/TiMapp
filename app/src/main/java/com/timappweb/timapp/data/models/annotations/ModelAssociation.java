package com.timappweb.timapp.data.models.annotations;

/**
 * Created by stephane on 5/9/2016.
 */
public @interface ModelAssociation {

    public enum SaveStrategy {
        REPLACE,
        APPEND
    }

    public enum Type {
        BELONGS_TO_MANY,
    }

    SaveStrategy saveStrategy() default SaveStrategy.REPLACE;

    Type type();


}
