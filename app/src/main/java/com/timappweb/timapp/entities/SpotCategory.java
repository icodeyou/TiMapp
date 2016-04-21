package com.timappweb.timapp.entities;

import android.util.Log;

import com.timappweb.timapp.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpotCategory implements Serializable{

    private static final String TAG = "SpotCategory";
    public int id;
    public String name;
    public String resource;


    public SpotCategory(String name) {
        this.name = name;
    }

    public static SpotCategory createDummy() {
        return new SpotCategory("DummyCategory");
    }
}
