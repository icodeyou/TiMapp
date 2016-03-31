package com.timappweb.timapp.entities;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;

public class Picture {

    public int id;
    public int created;
    public String photo;
    public String preview;
    public String square;
    public String photo_dir;
    public int place_id;
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
