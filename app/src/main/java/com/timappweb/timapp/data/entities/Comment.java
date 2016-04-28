package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by Jack on 28/04/2016.
 */
public class Comment implements Serializable {
    @Expose
    public String content;

    public Comment(String string) {
        this.content = string;
    }
}
