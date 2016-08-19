package com.timappweb.timapp.rest.callbacks;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.timappweb.timapp.fragments.EventPicturesFragment;
import com.timappweb.timapp.rest.io.responses.RestValidationError;
import com.timappweb.timapp.rest.io.responses.RestValidationErrorParser;

/**
 * Created by Stephane on 19/08/2016.
 */
public class FormErrorsCallback extends HttpCallback {

    private final Context context;
    private final String prefix;

    public FormErrorsCallback(Context context, String prefix) {
        super();
        this.context = context;
        this.prefix = prefix;
    }

    @Override
    public void badRequest(RestValidationError validationError) {
        RestValidationErrorParser errors = validationError.getErrors();

        if (errors.has(prefix + ".Quota")){
            Toast.makeText(this.context, errors.get(prefix+".Quota"), Toast.LENGTH_SHORT).show();
        }
    }
}
