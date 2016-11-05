package com.timappweb.timapp.data.models.exceptions;

import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by Stephane on 17/08/2016.
 */
public class CannotSaveModelException extends Exception {
    private MyModel model;

    public CannotSaveModelException(MyModel model) {
        super("Cannot save model: " + model);
        this.model = model;
    }


}
