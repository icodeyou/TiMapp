package com.timappweb.timapp.entities;

import com.google.gson.annotations.Expose;

public class Picture {

    @Expose
    public int id;

    @Expose
    public int created;

    @Expose
    public String photo;

    @Expose
    public String preview;

    @Expose
    public String square;

    @Expose
    public String photo_dir;

    @Expose
    public int place_id;

    @Expose
    public int user_id;


    public String getUrl(){
        return  this.photo_dir + "/" + this.photo;
    }
    public String getPreviewUrl(){
        return  this.photo_dir + "/" + this.preview;
    }
    public String getSquareUrl(){
        return  this.photo_dir + "/" + this.square;
    }

}
