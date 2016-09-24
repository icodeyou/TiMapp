package com.timappweb.timapp.auth;

/**
 * Created by Stephane on 24/09/2016.
 */
public class AuthManagerFactory {

    private AuthManagerFactory() {
    }

    public static AuthManager create(){
        return new AuthManager()
                .addAuthProvider(new FacebookAuthProvider());
    }

}
