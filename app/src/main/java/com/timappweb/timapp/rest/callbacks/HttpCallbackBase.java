package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
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
public class HttpCallbackBase<T> implements Callback<T> {

    private static final String TAG = "HttpCallbackBase";
    private List<HttpCallback<T>> callbacks = new LinkedList<>();
    private Response<T> response = null;


    public Response<T> getResponse() {
        return response;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        this.response = response;

        for (HttpCallback callback: callbacks){
            callback.setResponse(response);

            if (response.isSuccessful()){
                callback.successful(response.body());

                switch (response.code()){
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
            }
            else{
                callback.notSuccessful();
                // -----------------------------------------------------------------------------------------
                if (response.code() >= 400 && response.code() < 500){
                    callback.failure();

                    switch (response.code()){
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
                else if (response.code() >= 500 && response.code() < 600){
                    callback.error();

                    switch (response.code()){
                        case HttpURLConnection.HTTP_INTERNAL_ERROR:
                            callback.internalError();
                            break;
                    }
                }
            }

        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }


    // ---------------------------------------------------------------------------------------------
    public static <T> T parseErrorBody(Response response, Class<T> classOfT){

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

    public void add(HttpCallback httpCallback) {
        this.callbacks.add(httpCallback);
    }

    public void onResponse(Call<T> call) {
        this.onResponse(call, this.response);
    }
}
