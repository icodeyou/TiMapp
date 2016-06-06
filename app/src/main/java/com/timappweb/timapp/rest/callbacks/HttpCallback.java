package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.rest.model.RestValidationError;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 6/6/2016.
 */
public abstract class HttpCallback<T> {

    protected Response response;

    // --------------------------------------------------------------------------------------------
    public void successful(T feedback) {}
    public void ok(T feedback){}
    public void created(T feedback){}
    public void accepted(T feedback){}

    // --------------------------------------------------------------------------------------------
    public void notSuccessful() {}
    public void failure() {}
    public void badRequest(RestValidationError validationError){}
    public void forbidden(){}
    public void unauthorized(){}
    public void notFound(){}
    // --------------------------------------------------------------------------------------------
    public void error() {}
    public void internalError(){}

    public void setResponse(Response response) {
        this.response = response;
    }

}
