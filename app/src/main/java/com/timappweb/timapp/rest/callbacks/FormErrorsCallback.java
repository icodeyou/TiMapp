package com.timappweb.timapp.rest.callbacks;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.fragments.EventPicturesFragment;
import com.timappweb.timapp.rest.io.responses.RestValidationError;
import com.timappweb.timapp.rest.io.responses.RestValidationErrorParser;

/**
 * Created by Stephane on 19/08/2016.
 */
public class FormErrorsCallback extends HttpCallback {

    private final Context context;
    private final String prefix;
    private int toastFeedback;

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
        else if (toastFeedback != 0){
            Context context = MyApplication.getApplicationBaseContext();
            Toast.makeText(context, context.getString(this.toastFeedback), Toast.LENGTH_LONG).show();
        }
        else if (validationError.hasMessage()){
            Toast.makeText(context, validationError.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public FormErrorsCallback setToastFeedback(int toastFeedback) {
        this.toastFeedback = toastFeedback;
        return this;
    }
}
