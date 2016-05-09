package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

@Table(name = "Picture")
public class Picture extends SyncBaseModel {

    @Column(name = "Created")
    @Expose(serialize = false, deserialize = true)
    public int created;

    @Column(name = "Photo")
    @Expose(serialize = true, deserialize = true)
    public String photo;


    @Column(name = "Preview")
    @Expose(serialize = false, deserialize = true)
    public String preview;


    @Column(name = "Square")
    @Expose(serialize = false, deserialize = true)
    public String square;


    @Column(name = "PhotoDir")
    @Expose(serialize = false, deserialize = true)
    public String photo_dir;


    @Column(name = "Place")
    @Expose(serialize = false, deserialize = true)
    public Place place;

    @Column(name = "User")
    @Expose(serialize = false, deserialize = true)
    public User user;


    public String getUrl(){
        return  this.photo_dir + "/" + this.photo;
    }
    public String getPreviewUrl(){
        return  this.photo_dir + "/" + this.preview;
    }
    public String getSquareUrl(){
        return  this.photo_dir + "/" + this.square;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }
}
