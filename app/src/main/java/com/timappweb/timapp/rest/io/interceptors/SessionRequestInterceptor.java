package com.timappweb.timapp.rest.io.interceptors;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AppUpdateActivity;
import com.timappweb.timapp.auth.AuthManagerInterface;

import com.timappweb.timapp.config.server.ErrorCode;
import com.timappweb.timapp.config.server.ServerHeader;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.responses.ClientError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;

import static android.R.attr.versionCode;

/**
 * Created by stephane on 9/12/2015.
 *
 */
public class SessionRequestInterceptor implements Interceptor
{
    private final String appLanguage;
    private int versionCode;


    private static final String TAG = "Interceptor";
    private final AuthManagerInterface auth;

    public SessionRequestInterceptor(AuthManagerInterface authManager) {
        this.auth = authManager;
        try {
            Context context = MyApplication.getApplicationBaseContext();
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionCode = 0;
        }
        appLanguage = Locale.getDefault().getLanguage();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = this.auth.getToken();

        // Customize the request
        Request.Builder requestBuilder = original.newBuilder()
                .header(ServerHeader.XPLATFORM, "Android") // TODO constant
                .header(ServerHeader.XVERSION_CODE, String.valueOf(versionCode))
                .header(ServerHeader.XAPP_LANGUAGE, appLanguage)
                .method(original.method(), original.body());

        if (auth.isLoggedIn()) {
            setAuthHeader(requestBuilder, token);
        }

        Request request = requestBuilder.build();
        Response response = chain.proceed(request); //perform request, here original request will be executed

        Log.d(TAG, "HTTP Reponse code: " + response.code());
        if (response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED) { //if unauthorized
            Log.d(TAG, "Trying to refresh token... Waiting for synchronisation...");
            synchronized (RestClient.instance().getHttpClient()) { //perform all 401 in merge blocks, to avoid multiply token updates
                Log.d(TAG, "Synchronisation OK...");
                String currentToken = auth.getToken(); //get currently stored token

                if (currentToken == null){
                    logout();
                }
                else if (currentToken.equals(token)) { //compare current token with token that was stored before, if it was not updated - do update
                    int code = refreshToken() / 100; //refresh token
                    Log.d(TAG, "HTTP refreshing token: " + code);
                    if(code != 2) { //if refresh token failed for some reason
                        if(code == 4){
                            //only if response is 400, 500 might mean that token was not updated
                            logout(); //go to localLogin screen
                        }

                        return response; //if token refresh failed - simpleMessage error to user
                    }
                }

                if (auth.getToken() != null) { //retry requires new auth token,
                    Log.d(TAG, "retry request after refresh token...");
                    setAuthHeader(requestBuilder, auth.getToken()); //set auth token to updated
                    request = requestBuilder.build();
                    return chain.proceed(request); //repeat request with new token
                }
            }
        }
        else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN){
            try{
                Converter<ResponseBody, ClientError> errorConverter =
                        RestClient.instance().getRetrofit().responseBodyConverter(ClientError.class, new Annotation[0]);
                ResponseBody body = response.body();
                ClientError clientError = errorConverter.convert(body);
                if (clientError.getAppCode() == ErrorCode.UNAUTHORIZED_VERSION){
                    RestClient.instance().cancelCalls();
                    Context context = MyApplication.getApplicationBaseContext();
                    Intent intent = new Intent(context, AppUpdateActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
            catch (Exception ex){
                Log.e(TAG, "Exception while parsing response: " + ex.getMessage());
                if (BuildConfig.DEBUG){
                    ex.printStackTrace();
                }
            }
        }

        return response;
    }


    private void setAuthHeader(Request.Builder builder, String token) {
        Log.d(TAG, "Request interceptor: User is logged in with token " + token);
        //HttpUrl newUrl = url.newBuilder()
        //        .addQueryParameter("_token", token)
        //        .build();

        builder
                //.url(newUrl)
                .header("Authorization", String.format("Bearer %s", token));
    }

    private int refreshToken() {
        // TODO
        //Refresh token, synchronously, save it, and return result code
        //you might use retrofit here
        return 404;
    }

    private void logout() {
        RestClient.instance().logoutUser();
    }

}

