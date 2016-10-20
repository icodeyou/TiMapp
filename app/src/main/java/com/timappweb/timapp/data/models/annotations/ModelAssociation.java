package com.timappweb.timapp.data.models.annotations;

import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by stephane on 5/9/2016.
 */
public @interface ModelAssociation {

    String remoteForeignKey() default ""; // TODO

    enum SaveStrategy {
        REPLACE,
        APPEND
    }

    enum Type {
        BELONGS_TO_MANY, BELONGS_TO,
    }

    Class<? extends MyModel> joinModel();

    SaveStrategy saveStrategy() default SaveStrategy.REPLACE;

    Type type();

    Class<?> targetModel() default Object.class;
}
