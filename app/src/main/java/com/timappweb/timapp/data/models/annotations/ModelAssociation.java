package com.timappweb.timapp.data.models.annotations;

import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by stephane on 5/9/2016.
 */
public @interface ModelAssociation {

    enum SaveStrategy {
        REPLACE,
        APPEND
    }

    enum Type {
        BELONGS_TO_MANY, BELONGS_TO,
    }

    String remoteForeignKey() default ""; // TODO

    Class<? extends MyModel> joinModel();

    SaveStrategy saveStrategy() default SaveStrategy.REPLACE;

    Type type();

    Class<?> targetModel() default Object.class;

    Class<?> targetTable() default Object.class;


}
