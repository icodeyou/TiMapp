package com.timappweb.timapp.services;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.timappweb.timapp.config.Constants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by stephane on 4/3/2016.
 */
public class InstanceIDService extends InstanceIDListenerService {

    public static String scope = "GCM"; // e.g. communicating using GCM, but you can use any
    private final InstanceID iid;
    private ArrayList<TokenItem> tokens;

    public static String getId(Context context){
        return InstanceID.getInstance(context).getId();
    }

    public static String getToken(Context context) throws IOException {
        return InstanceID.getInstance(context).getToken(Constants.GOOGLE_PROJECT_ID, scope);
    }

    public static void deleteToken(Context context) throws IOException {
        InstanceID.getInstance(context).deleteToken(Constants.GOOGLE_PROJECT_ID, scope);
    }

    public TokenItem create() throws IOException {
        TokenItem tokenItem = new TokenItem();
        iid.getToken(Constants.GOOGLE_PROJECT_ID, scope);
        return tokenItem;
    }

    public void onTokenRefresh() {
        refreshAllTokens();
    }

    public InstanceIDService() {
        this.tokens = new ArrayList<>();
        iid = InstanceID.getInstance(this);
    }

    private void refreshAllTokens() {
        // assuming you have defined TokenList as
        // some generalized store for your tokens
        for(TokenItem tokenItem : this.tokens) {
            try {
                tokenItem.token =
                        iid.getToken(tokenItem.authorizedEntity,tokenItem.scope,tokenItem.options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // send this tokenItem.token to your server
        }
    }

    private class TokenItem {
        public String token;
        public String authorizedEntity;
        public java.lang.String scope;
        public Bundle options;
    }
}
