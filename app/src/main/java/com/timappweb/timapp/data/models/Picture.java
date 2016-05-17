package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

@Table(name = "Picture")
public class Picture extends SyncBaseModel {

    // =============================================================================================
    // DATABASE

    @Column(name = "Created")
    @Expose(serialize = false, deserialize = true)
    public int created;

    @Column(name = "Photo", notNull = true)
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

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Place", notNull = true)
    @Expose(serialize = false, deserialize = true)
    public Place place;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User", notNull = false)
    @Expose(serialize = false, deserialize = true)
    public User user;

    @Column(name = "BaseUrl")
    public String base_url;

    // =============================================================================================
    // Fields

    // =============================================================================================

    public Picture() {
    }

    // =============================================================================================

    public String getUrl(){
        return  this.base_url + "/" + this.photo_dir + "/" + this.photo;
    }

    public String getPreviewUrl(){
        return  this.base_url + "/" + this.photo_dir + "/" + this.preview;
    }
    public String getSquareUrl(){
        return  this.base_url + "/" + this.photo_dir + "/" + this.square;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", created=" + created +
                ", photo='" + photo + '\'' +
                ", preview='" + preview + '\'' +
                ", square='" + square + '\'' +
                ", photo_dir='" + photo_dir + '\'' +
                ", base_url='" + base_url + '\'' +
                ", place=" + place +
                ", user=" + user +
                '}';
    }
}
