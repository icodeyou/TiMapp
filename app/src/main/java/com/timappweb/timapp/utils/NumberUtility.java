package com.timappweb.timapp.utils;

/**
 * Created by stephane on 6/4/2016.
 */
public class NumberUtility {

    public static String displayCount(Integer value){
        if (value == null) return "0";

        if (value > 1000){
            return value/1000 + "M";
        }
        else{
            return String.valueOf(value);
        }
    }
}
