package com.timappweb.timapp.utils.deeplinks;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.config.IntentsUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/**
 * Created by Stephane on 08/10/2016.
 */

public class DeepLinkParser {

    private static final String TAG = "DeepLinkParser";
    private LinkedList<DeepLinkCallback> matchersCallback;
    private NoMatchCallback noMatchCallback;

    public DeepLinkParser setNoMatchCallback(NoMatchCallback noMatchCallback) {
        this.noMatchCallback = noMatchCallback;
        return this;
    }

    public DeepLinkParser addMatcher(DeepLinkCallback callback) {
        this.matchersCallback.add(callback);
        return this;
    }

    public DeepLinkParser() {
        this.matchersCallback = new LinkedList<>();
    }

    public void parse(GoogleApiClient mGoogleApiClient, Activity activity, boolean autoLaunchDeepLink){
        Log.d(TAG, "Init deep link parsing");
        AppInvite.AppInviteApi
                .getInvitation(mGoogleApiClient, activity, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    Log.d(TAG, "Processing " + deepLink);
                                    try {
                                        deepLink = new URL(deepLink).getPath();
                                     // Extract deep link from Intent
                                        for (DeepLinkCallback callback: matchersCallback){
                                            try{
                                                UrlParser urlParser = new UrlParser(deepLink, callback.getMask());
                                                if (callback.onMatch(urlParser)) {
                                                    return;
                                                }
                                            } catch (UrlParser.NotMatchingURLException e) {
                                                Log.e(TAG, "Received deep link with invalid url: " + e.getMessage());
                                            }
                                        }
                                    } catch (MalformedURLException e) {
                                        Log.e(TAG, "Cannot parse url: " + e.getMessage());
                                    }
                                } else {
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }

                                if (DeepLinkParser.this.noMatchCallback != null){
                                    DeepLinkParser.this.noMatchCallback.onNoMatch();
                                }
                            }
                        });
    }

    // ---------------------------------------------------------------------------------------------

    public interface NoMatchCallback{
        void onNoMatch();
    }

    public interface DeepLinkCallback{

        boolean onMatch(UrlParser parser);

        String getMask();

    }

}
