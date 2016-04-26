package com.timappweb.timapp.services;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import java.io.IOException;

/**
 * Created by stephane on 4/3/2016.
 */
public class MyInstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDListenerService";
    private final InstanceID iid;
    //private ArrayList<TokenItem> tokens;

    public void onTokenRefresh() {
        refreshAllTokens();
    }

    public MyInstanceIDListenerService() {
      //  this.tokens = new ArrayList<>();
        iid = InstanceID.getInstance(this);
    }

    private void refreshAllTokens() {
        // assuming you have defined TokenList as
        // some generalized store for your tokens
        //for(TokenItem tokenItem : this.tokens) {
            try {
                String token = iid.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                MyApplication.updateGoogleMessagingToken(getApplicationContext(), token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // send this tokenItem.token to your server
        //}
    }
    
    
/*
    private class TokenItem {
        public String token;
        public String authorizedEntity;
        public java.lang.String scope;
        public Bundle options;
    }*/
}
