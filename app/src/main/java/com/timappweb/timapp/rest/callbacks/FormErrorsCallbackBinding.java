package com.timappweb.timapp.rest.callbacks;

import android.databinding.ViewDataBinding;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.rest.model.RestValidationError;
import com.timappweb.timapp.rest.model.RestValidationErrorParser;
import com.timappweb.timapp.utils.Util;

import java.lang.reflect.Method;

/**
 * Created by stephane on 6/6/2016.
 */
public class FormErrorsCallbackBinding extends HttpCallback {

    private static final String TAG = "FormErrorsCallback";

    private final ViewDataBinding viewBinding;

    public FormErrorsCallbackBinding(ViewDataBinding viewBinding) {
        this.viewBinding = viewBinding;
    }

    @Override
    public void badRequest(RestValidationError validationError) {
        if (validationError != null){
            try {
                Method method = viewBinding.getClass().getMethod("setErrors", validationError.getClass());
                method.invoke(viewBinding, validationError);
            } catch (Exception e) {
                Util.appStateError(TAG, "There should have a setErrors() method for this binding");
            }
        }
    }
}
