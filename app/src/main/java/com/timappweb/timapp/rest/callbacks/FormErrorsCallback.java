package com.timappweb.timapp.rest.callbacks;

import android.databinding.ViewDataBinding;

import com.timappweb.timapp.rest.model.RestValidationError;
import com.timappweb.timapp.rest.model.RestValidationErrorParser;

import java.lang.reflect.Method;

/**
 * Created by stephane on 6/6/2016.
 */
public class FormErrorsCallback extends HttpCallback {

    private final ViewDataBinding viewBinding;

    public FormErrorsCallback(ViewDataBinding viewBinding) {
        this.viewBinding = viewBinding;
    }

    @Override
    public void badRequest(RestValidationError validationError) {
        if (validationError != null){
            RestValidationErrorParser errors = validationError.getErrors();
            if (errors != null) {
                try {
                    Method method = viewBinding.getClass().getMethod("setErrors", errors.getClass());
                    method.invoke(viewBinding, errors);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
