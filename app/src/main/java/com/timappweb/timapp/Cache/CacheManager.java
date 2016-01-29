package com.timappweb.timapp.Cache;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by stephane on 1/29/2016.
 */
public class CacheManager {


    private final BufferedWriter out;
    private final BufferedReader in;

    public CacheManager(Context context) throws IOException {
        File cacheDir;
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(),"MyCustomObject");
        else
            cacheDir = context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();

        out = new BufferedWriter(new FileWriter(cacheDir), 1024);
        in = new BufferedReader(new FileReader(cacheDir), 1024);
    }

    public Object read(){
        return null;
    }
    public void write(String name, String value){

    }
}
