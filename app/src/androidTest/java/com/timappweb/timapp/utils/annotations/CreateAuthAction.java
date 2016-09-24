package com.timappweb.timapp.utils.annotations;

import com.timappweb.timapp.auth.FacebookAuthProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Stephane on 24/09/2016.
 */
@Retention( value = RetentionPolicy.RUNTIME)
@Target( value = { ElementType.METHOD})
public @interface CreateAuthAction {

    boolean replaceIfExists() default false; // Replace the auth user if he is already logged in

    String providerId() default FacebookAuthProvider.PROVIDER_ID;

    String payloadId() default "facebook_user";

}
