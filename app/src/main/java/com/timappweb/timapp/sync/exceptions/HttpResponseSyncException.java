package com.timappweb.timapp.sync.exceptions;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import java.net.HttpURLConnection;
import java.util.HashMap;

import retrofit2.Response;

/**
 * Created by Stephane on 18/08/2016.
 */
public class HttpResponseSyncException extends CannotSyncException {

    private final Response response;
    private final HashMap<String, String> options;


    public <T> HttpResponseSyncException(Response<T> response, HashMap<String, String> options) {
        super("HTTP response code is invalid: " + response.code(), 0);
        this.response = response;
        this.options = options;
    }

    @Override
    public String toString() {
        return "HttpResponseSyncException{" +
                "message=" + getMessage() +
                "response=" + response +
                "options=" + options +
                '}';
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public String getUserFeedback() {
        switch (response.code()){
            case HttpURLConnection.HTTP_FORBIDDEN:
                return MyApplication.getApplicationBaseContext().getString(R.string.error_require_login);
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_BAD_GATEWAY:
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
            default:
                return MyApplication.getApplicationBaseContext().getString(R.string.error_server_unavailable);
        }
    }
}
