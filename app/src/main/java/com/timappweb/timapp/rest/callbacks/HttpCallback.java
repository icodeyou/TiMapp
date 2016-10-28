package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.rest.io.responses.ClientError;
import com.timappweb.timapp.rest.io.responses.RestValidationError;

import retrofit2.Response;

/**
 * Created by stephane on 6/6/2016.
 */
public abstract class HttpCallback<T> {

    protected Response<T> response;

    // --------------------------------------------------------------------------------------------
    // 20X

    /**
     * Any 200-299 response
     * @param feedback
     */
    public void successful(T feedback) throws Exception{}

    /**
     * 200 response
     * @param feedback
     */
    public void ok(T feedback){}

    /**
     * 201
     * @param feedback
     */
    public void created(T feedback){}
    /**
     * 202
     * @param feedback
     */
    public void accepted(T feedback){}

    // --------------------------------------------------------------------------------------------
    /**
     * Any response where status code is not 200-299
     */
    public void notSuccessful() {}
    // --------------------------------------------------------------------------------------------
    // 400

    public void failure(ClientError clientError) {}
    public void badRequest(RestValidationError validationError){}
    public void forbidden(ClientError clientError){}
    public void unauthorized(){}
    public void notFound(){}

    // --------------------------------------------------------------------------------------------
    // 500
    public void error() {}
    public void internalError(){}

    // --------------------------------------------------------------------------------------------
    public void setResponse(Response<T> response) {
        this.response = response;
    }

}
