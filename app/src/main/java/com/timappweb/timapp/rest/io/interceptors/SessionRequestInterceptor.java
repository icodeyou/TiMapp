package com.timappweb.timapp.rest.io.interceptors;

import android.util.Log;

import com.timappweb.timapp.config.AuthProviderInterface;
import com.timappweb.timapp.config.ServerHeader;
import com.timappweb.timapp.rest.RestClient;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by stephane on 9/12/2015.
 *
 */
public class SessionRequestInterceptor implements Interceptor
{

    private static final String TAG = "Interceptor";
    private final AuthProviderInterface auth;

    public SessionRequestInterceptor(AuthProviderInterface authProvider) {
        this.auth = authProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = this.auth.getToken();
        String providerToken = this.auth.getSocialProviderToken();

        // Customize the request
        Request.Builder requestBuilder = original.newBuilder()
                .header(ServerHeader.XPLATFORM, "Android")
                .method(original.method(), original.body());

        if (auth.isLoggedIn()) {
            setAuthHeader(requestBuilder, token, providerToken, original.url());
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
                            logout(); //go to login screen
                        }

                        return response; //if token refresh failed - show error to user
                    }
                }

                if (auth.getToken() != null) { //retry requires new auth token,
                    Log.d(TAG, "retry request after refresh token...");
                    setAuthHeader(requestBuilder, auth.getToken(), providerToken, original.url()); //set auth token to updated
                    request = requestBuilder.build();
                    return chain.proceed(request); //repeat request with new token
                }
            }
        }

        return response;
    }


    private void setAuthHeader(Request.Builder builder, String token, String providerToken, HttpUrl url) {
        Log.d(TAG, "Request interceptor: User is logged in with token " + token);
        //HttpUrl newUrl = url.newBuilder()
        //        .addQueryParameter("_token", token)
        //        .build();

        builder
                //.url(newUrl)
                .header("Authorization", String.format("Bearer %s", token));
        if (providerToken != null){
            builder.header("SocialAccessToken", providerToken);
        }

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

