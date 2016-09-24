package com.timappweb.timapp.utils.annotations;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Stephane on 24/09/2016.
 */
@Retention( value = RetentionPolicy.RUNTIME)
@Target( value = { ElementType.METHOD})
public @interface ConfigState {

    boolean rules() default true;

    boolean spotCategories() default true;

    boolean eventCategories() default true;
}
