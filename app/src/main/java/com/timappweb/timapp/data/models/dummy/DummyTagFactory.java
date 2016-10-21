package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.utils.Util;

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
        return "Tag" + UUID.randomUUID().toString().substring(0,10);
    }
}
