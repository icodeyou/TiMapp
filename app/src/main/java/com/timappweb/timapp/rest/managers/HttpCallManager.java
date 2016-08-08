package com.timappweb.timapp.rest.managers;

import com.timappweb.timapp.rest.callbacks.HttpCallbackBase;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 6/6/2016.
 */
public class HttpCallManager {

    private final HttpCallbackBase<Object> callbackBase;
    private final Call call;

    public HttpCallManager(Call call) {
        this.call = call;
        this.callbackBase = new HttpCallbackBase<>();
    }

    public HttpCallManager onResponse(HttpCallback httpCallback) {
        if (this.callbackBase.isDone()){
            this.callbackBase.onResponse(call);
        }
        else{
            this.callbackBase.add(httpCallback);
        }
        return this;
    }
    public HttpCallManager onError(RequestFailureCallback requestFailureCallback) {
        if (this.callbackBase.isDone()){
            this.callbackBase.onFailure(call);
        }
        else{
            this.callbackBase.add(requestFailureCallback);
        }
        return this;
    }

    /**
     * ASYNC execute
     * @return
     */
    public HttpCallManager perform(){
        this.call.enqueue(this.callbackBase);
        return this;
    }

    /**
     * SYNC execute
     * @return
     */
    public Response execute() throws IOException {
        try{
            Response response = this.call.execute();
            this.callbackBase.onResponse(this.call, response);
            return response;
        }
        catch (IOException e) {
            this.callbackBase.onFailure(this.call, e);
            throw e;
        }
    }

    public void cancel(){
        this.call.cancel();
    }

    public Response getResponse() {
        return this.callbackBase.getResponse();
    }

    public boolean hasError() {
        return this.callbackBase.getError() != null;
    }

    public boolean hasResponse() {
        return this.callbackBase.getResponse() != null;
    }

    public Call getCall() {
        return call;
    }
}
