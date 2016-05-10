package com.timappweb.timapp.data.models.annotations;

import com.timappweb.timapp.data.models.UserTag;

/**
 * Created by stephane on 5/9/2016.
 */
public @interface ModelAssociation {

    enum SaveStrategy {
        REPLACE,
        APPEND
    }

    enum Type {
        BELONGS_TO_MANY,
    }

    Class<UserTag> joinModel();

    SaveStrategy saveStrategy() default SaveStrategy.REPLACE;

    Type type();


}
