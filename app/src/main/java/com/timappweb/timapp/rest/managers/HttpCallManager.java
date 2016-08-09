package com.timappweb.timapp.rest.managers;

import com.timappweb.timapp.rest.callbacks.HttpCallbackBase;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 6/6/2016.
 */
public class HttpCallManager<T> {

    private final HttpCallbackBase<T> callbackBase;
    private final Call<T> call;
    private long callDelay;
    private Timer timer;

    public HttpCallManager(Call<T> call) {
        this.call = call;
        this.callDelay = 0;
        this.callbackBase = new HttpCallbackBase<>();
    }

    public HttpCallManager<T> onResponse(HttpCallback<T> httpCallback) {
        if (this.callbackBase.isDone()){
            this.callbackBase.onResponse(call);
        }
        else{
            this.callbackBase.add(httpCallback);
        }
        return this;
    }
    public HttpCallManager<T> onError(RequestFailureCallback requestFailureCallback) {
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
    public HttpCallManager<T> perform(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                HttpCallManager.this.call.enqueue(HttpCallManager.this.callbackBase);
            }
        }, this.callDelay);

        return this;
    }

    /**
     * SYNC execute
     * @return
     */
    public Response<T> execute() throws IOException {
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
        if (timer != null)
            timer.cancel();

        this.call.cancel();
    }

    public Response<T> getResponse() {
        return this.callbackBase.getResponse();
    }

    public boolean hasError() {
        return this.callbackBase.getError() != null;
    }

    public boolean hasResponse() {
        return this.callbackBase.getResponse() != null;
    }

    public Call<T> getCall() {
        return call;
    }

    public HttpCallManager<T> setCallDelay(long callDelay) {
        this.callDelay = callDelay;
        return this;
    }
}
