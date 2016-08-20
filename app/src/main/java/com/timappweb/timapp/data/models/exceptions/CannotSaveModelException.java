package com.timappweb.timapp.data.models.exceptions;

import com.activeandroid.Model;
import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by Stephane on 17/08/2016.
 */
public class CannotSaveModelException extends Exception {
    private Model model;

    public CannotSaveModelException(Model model) {
        super("Cannot save model: " + model);
        this.model = model;
    }


}
