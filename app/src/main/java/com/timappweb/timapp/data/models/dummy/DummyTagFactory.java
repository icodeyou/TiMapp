package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.utils.Util;

import java.util.Random;
import java.util.UUID;

/**
 * Created by stephane on 5/25/2016.
 */
public class DummyTagFactory {

    public static Tag create(){
        Tag tag = new Tag();
        tag.name = uniqName();
        tag.count_ref = 12;
        return tag;
    }

    public static String uniqName() {
        return "Tag" + randomString(10);
    }

    public static String invalidName() {
        return UUID.randomUUID().toString().substring(0,10);
    }

    public static char randomChar(){
        Random r = new Random();
        return (char)(r.nextInt(26) + 'a');
    }

    public static String randomString(int size){
        String res = "";
        for (int i = 0; i < size; i++){
            res += randomChar();
        }
        return res;
    }
}
