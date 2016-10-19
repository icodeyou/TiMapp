package com.timappweb.timapp.rest.managers;

import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallbackGroup;
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
public class HttpCallManager<T> implements RestClient.Cancelable{

    private final HttpCallbackGroup<T> callbackGroup;
    private Call<T> call;
    private long callDelay;
    private Timer timer;

    public HttpCallManager(Call<T> call) {
        this.call = call;
        this.callDelay = 0;
        this.callbackGroup = new HttpCallbackGroup<>(this.call);
    }

    public HttpCallManager<T> onResponse(HttpCallback<T> httpCallback) {
        if (this.callbackGroup.isDone() && !this.callbackGroup.isFailed()){
            this.callbackGroup.onResponse(call);
        }
        else{
            this.callbackGroup.add(httpCallback);
        }
        return this;
    }
    public HttpCallManager<T> onError(RequestFailureCallback requestFailureCallback) {
        if (this.callbackGroup.isDone() && this.callbackGroup.isFailed()){
            this.callbackGroup.onFailure(call);
        }
        else{
            this.callbackGroup.add(requestFailureCallback);
        }
        return this;
    }

    public HttpCallManager onFinally(FinallyCallback<T> callback) {
        if (this.callbackGroup.isDone()){
            this.callbackGroup.onFinally(callback);
        }
        else{
            this.callbackGroup.add(callback);
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
                HttpCallManager.this.call.enqueue(HttpCallManager.this.callbackGroup);
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
            this.callbackGroup.onResponse(this.call, response);
            return response;
        }
        catch (IOException e) {
            this.callbackGroup.onFailure(this.call, e);
            throw e;
        }
    }

    public void cancel(){
        if (timer != null)
            timer.cancel();

        this.call.cancel();
    }

    public Response<T> getResponse() {
        return this.callbackGroup.getResponse();
    }

    public boolean hasError() {
        return this.callbackGroup.getError() != null;
    }

    public boolean hasResponse() {
        return this.callbackGroup.getResponse() != null;
    }

    public Call<T> getCall() {
        return call;
    }

    public HttpCallManager<T> setCallDelay(long callDelay) {
        this.callDelay = callDelay;
        return this;
    }
    public void setResponse(Response<T> r) {
        this.callbackGroup.setResponse(r);
    }

    public boolean isDone() {
        return callbackGroup.isDone();
    }

    public void retry() {
        this.cancel();
        this.call = this.call.clone();
        this.perform();
    }

    public interface FinallyCallback<ResponseBodyType>{
        void onFinally(Response<ResponseBodyType> response, Throwable error);
    }

    public interface StartCallback<ResponseBodyType>{
        void onStart();
    }
}
