package com.timappweb.timapp.utils;

import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Stephane on 10/09/2016.
 */
public abstract class DummyHttpRequestCall<T> implements  Call{

    @Override
    public void enqueue(Callback callback) {
        try {
            callback.onResponse(this, this.execute());
        } catch (IOException e) {
            callback.onFailure(this, e);
        }
    }

    @Override
    public boolean isExecuted() {
        return true;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<ResponseSyncWrapper> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }
}


