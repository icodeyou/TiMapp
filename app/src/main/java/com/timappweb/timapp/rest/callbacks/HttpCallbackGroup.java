package com.timappweb.timapp.rest.callbacks;

import android.util.Log;
import android.util.MalformedJsonException;
import android.widget.Toast;

import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.R;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.io.responses.RestValidationError;

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
public class HttpCallbackGroup<ResponseBodyType> implements Callback<ResponseBodyType> {

    private static final String TAG = "HttpCallbackGroup";
    private final Call<ResponseBodyType> call;
    private List<HttpCallback<ResponseBodyType>> responseCallbacks = new LinkedList<>();
    private List<RequestFailureCallback> failureCallbacks = new LinkedList<>();
    private List<HttpCallManager.FinallyCallback> finallyCallbacks = new LinkedList<>();

    private Response<ResponseBodyType> response = null;
    private Throwable error = null;
    private RestValidationError validationErrors;

    public HttpCallbackGroup(Call<ResponseBodyType> call) {
        this.call = call;
    }


    public Response<ResponseBodyType> getResponse() {
        return response;
    }

    @Override
    public void onResponse(Call<ResponseBodyType> call, Response<ResponseBodyType> response) {
        this.response = response;

        if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST){
            validationErrors = parseErrorBody(response, RestValidationError.class);//this.getValidationErrors();
        }

        try{
            for (HttpCallback<ResponseBodyType> callback : responseCallbacks) {
                callback.setResponse(response);
                this.dispatchResponse(this.response, callback, this.validationErrors);
            }
        }
        catch (Throwable ex){
            Log.e(TAG, "Exception while dispatching response: " + ex);
            this.error = ex;
            if (BuildConfig.DEBUG){
                ex.printStackTrace();
            }
        }
        this.callFinallyCallbacks();
    }

    private void callFinallyCallbacks() {
        for (HttpCallManager.FinallyCallback callback : finallyCallbacks) {
            callback.onFinally(response, error);
        }
    }

    public static <ResponseBodyType> void dispatchResponse(Response<ResponseBodyType> response,
                                                           HttpCallback<ResponseBodyType> callback,
                                                           RestValidationError validationErrors)
        throws Throwable{

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
                            callback.badRequest(validationErrors);
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

    public void onResponse(Call<ResponseBodyType> call) {
        this.onResponse(call, this.response);
    }

    @Override
    public void onFailure(Call<ResponseBodyType> call, Throwable error) {
        this.error = error;
        Log.e(TAG, "Request error: " + error);
        //error.printStackTrace();
        for (RequestFailureCallback callback : failureCallbacks) {
            if (!this.call.isCanceled()){
                dispatchError(error, callback);
            }
        }
        this.callFinallyCallbacks();
    }


    public void onFailure(Call<ResponseBodyType> call) {
        this.onFailure(call, this.error);
    }

    public void add(HttpCallback<ResponseBodyType> httpCallback) {
        this.responseCallbacks.add(httpCallback);
    }

    public void add(RequestFailureCallback callback) {
        this.failureCallbacks.add(callback);
    }

    public void add(HttpCallManager.FinallyCallback callback) {
        this.finallyCallbacks.add(callback);
    }


    // ---------------------------------------------------------------------------------------------
    private static <ErrorType> ErrorType parseErrorBody(Response response, Class<ErrorType> classOfT) {

        if (response.errorBody() != null) {
            Converter<ResponseBody, ErrorType> errorConverter =
                    RestClient.instance().getRetrofit().responseBodyConverter(classOfT, new Annotation[0]);
            try {
                return errorConverter.convert(response.errorBody());
            } catch (IOException e) {
                Log.e(TAG, "Cannot convert error body from rest response: " + e.getMessage());
                if (BuildConfig.DEBUG){
                    e.printStackTrace();
                }
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
        else if (error instanceof MalformedJsonException){
            callback.unexpectedFormat(error);
        }
    }

    public void onFinally(HttpCallManager.FinallyCallback<ResponseBodyType> callback) {
        callback.onFinally(response, error);
    }

    public RestValidationError getValidationErrors() {
        if (validationErrors == null) {
            validationErrors = parseErrorBody(this.response, RestValidationError.class);
        }
        return validationErrors;
    }

    public boolean isFailed() {
        return error != null;
    }

    public void setResponse(Response<ResponseBodyType> response) {
        this.response = response;
    }
}

