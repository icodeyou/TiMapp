package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.model.RestValidationError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by stephane on 6/6/2016.
 */
public class HttpCallbackGroup<T> implements Callback<T> {

    private static final String TAG = "HttpCallbackGroup";
    private List<HttpCallback<T>> responseCallbacks = new LinkedList<>();
    private List<RequestFailureCallback> failureCallbacks = new LinkedList<>();
    private List<HttpCallManager.FinallyCallback> finallyCallbacks = new LinkedList<>();

    private Response<T> response = null;
    private Throwable error = null;


    public Response<T> getResponse() {
        return response;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        this.response = response;

        for (HttpCallback callback : responseCallbacks) {
            callback.setResponse(response);
            HttpCallbackGroup.dispatchResponse(response, callback);
        }
        this.callFinallyCallbacks(false);
    }

    private void callFinallyCallbacks(boolean isFailure) {
        for (HttpCallManager.FinallyCallback callback : finallyCallbacks) {
            callback.onFinally(isFailure);
        }
    }

    public static void dispatchResponse(Response response, HttpCallback callback) {
        if (response.isSuccessful()) {
            callback.successful(response.body());

            switch (response.code()) {
                case HttpURLConnection.HTTP_OK:
                    callback.ok(response.body());
                    break;
                case HttpURLConnection.HTTP_CREATED:
                    callback.created(response.body());
                    break;
                case HttpURLConnection.HTTP_ACCEPTED:
                    callback.accepted(response.body());
                    break;
            }
        } else {
            callback.notSuccessful();
            // -----------------------------------------------------------------------------------------
            if (response.code() >= 400 && response.code() < 500) {
                callback.failure();

                switch (response.code()) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        RestValidationError errors = parseErrorBody(response, RestValidationError.class);
                        callback.badRequest(errors);
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        callback.forbidden();
                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        callback.unauthorized();
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        callback.notFound();
                        break;
                }
            }
            // -----------------------------------------------------------------------------------------
            else if (response.code() >= 500 && response.code() < 600) {
                callback.error();

                switch (response.code()) {
                    case HttpURLConnection.HTTP_INTERNAL_ERROR:
                        callback.internalError();
                        break;
                    // TODO add more
                }
            }
        }
    }

    public void onResponse(Call<T> call) {
        this.onResponse(call, this.response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable error) {
        this.error = error;
        Log.d(TAG, "Request error: " + error);
        error.printStackTrace();
        for (RequestFailureCallback callback : failureCallbacks) {
            dispatchError(error, callback);
        }
        this.callFinallyCallbacks(true);
    }


    public void onFailure(Call<T> call) {
        this.onFailure(call, this.error);
    }

    public void add(HttpCallback httpCallback) {
        this.responseCallbacks.add(httpCallback);
    }

    public void add(RequestFailureCallback callback) {
        this.failureCallbacks.add(callback);
    }

    public void add(HttpCallManager.FinallyCallback callback) {
        this.finallyCallbacks.add(callback);
    }


    // ---------------------------------------------------------------------------------------------
    private static <T> T parseErrorBody(Response response, Class<T> classOfT) {

        if (response.errorBody() != null) {
            Converter<ResponseBody, T> errorConverter =
                    RestClient.instance().getRetrofit().responseBodyConverter(classOfT, new Annotation[0]);
            try {
                Log.v(TAG, "Received errorBody=" + response.errorBody().string());
                return errorConverter.convert(response.errorBody());
            } catch (IOException e) {
                Log.e(TAG, "Cannot convert error body from rest response: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isDone() {
        return response != null || error != null;
    }

    public Throwable getError() {
        return error;
    }

    public static void dispatchError(Throwable error, RequestFailureCallback callback){
        callback.onError(error);
        if (error instanceof IOException) {
            callback.network((IOException) error);
        }
    }

    public void onFinally(HttpCallManager.FinallyCallback callback) {
        callback.onFinally(error != null);
    }
}

