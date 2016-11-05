package com.timappweb.timapp.data.models;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.util.ArrayList;
import java.util.List;

@Table(database = AppDatabase.class)
public class EventPost extends SyncBaseModel{

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = User.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Expose
    public User user;

    @Column
    @Expose
    public double latitude;

    @Column
    @Expose
    public double longitude;

    //@Column(name = "Comment")
    //@Expose
    //public String comment;

    //@Column(name = "Anonymous")
    //@Expose
    //public boolean anonymous;
    // =============================================================================================
    // FIELDS

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO_MANY,
            joinModel = PostTag.class,
            saveStrategy = ModelAssociation.SaveStrategy.REPLACE,
            targetModel = Tag.class,
            targetTable = PostTag_Table.class,
            remoteForeignKey = "event_post_id")
    @Expose
    public List<Tag> tags;

    // =============================================================================================

    //public String address = "";

    public Event event;

    // =============================================================================================
    // GETTERS


    public void setTags(List<Tag> tags){
        this.tags = tags;
    }

    public EventPost() {
        this.tags = new ArrayList<>();
    }

    public LatLng getLocation() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLocation(@NonNull Location location){
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    @Override
    public String toString() {
        return "EventPost{" +
                "id=" + id +
                ", user=" + user +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", created=" + created +
                ", tag_number=" + (tags != null ? tags.size(): 0) +
                ", tags='" + tags + '\'' +
                '}';
    }


    public List<Tag> getTags() {
        return tags;
    }


    public boolean validateForSubmit() {
        if (!this.hasTags()) {
            return false;
        }
        return true;
    }

    /*
    public String getUsername() {
        return user != null
                ? (anonymous ? "Anonymous" : user.username)
                : "Former user";
    }*/


    public User getUser() {
        return user;
    }

    public boolean hasTags() {
        return tags != null && tags.size() > 0;
    }

    //public String getComment() {
    //    return comment;
    //}

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }

    @Override
    public int getSyncType() {
        //return DataSyncAdapter.SYNC_TYPE_
        throw new InternalError("Not syncable element");
    }

}