package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

@Table(name = "Picture")
public class Picture extends SyncBaseModel {

    public enum ThumbnailType{ CARD, SQUARE }

    // =============================================================================================
    // DATABASE

    /*
    @Column(name = "Photo", notNull = true)
    @Expose(serialize = true, deserialize = true)
    public String photo;*/
    @Column(name = "OriginalUrl", notNull = false)
    @Expose(serialize = true, deserialize = true)
    public String original;

    @Column(name = "Preview")
    @Expose(serialize = false, deserialize = true)
    @SerializedName("thumbnail_card")
    public String card;

    @Column(name = "Square")
    @Expose(serialize = false, deserialize = true)
    @SerializedName("thumbnail_square")
    public String square;

    /*
    @Column(name = "PhotoDir", notNull = true)
    @Expose(serialize = false, deserialize = true)
    public String photo_dir;*/

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Event", notNull = true, onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("place")
    @Expose(serialize = false, deserialize = true)
    public Event event;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User", notNull = false, onDelete = Column.ForeignKeyAction.SET_NULL, onUpdate = Column.ForeignKeyAction.CASCADE)
    @Expose(serialize = false, deserialize = true)
    public User user;

    /*
    @Column(name = "BaseUrl")
    @Expose(serialize = false, deserialize = true)
    public String base_url;*/

    // =============================================================================================
    // Fields

    // =============================================================================================

    public Picture() {
    }

    // =============================================================================================

    public String getUrl(){
        return  this.original;
    }

    public String getThumbnailUrl(ThumbnailType type){
        switch (type){
            case SQUARE:
                return this.square;
            case CARD:
            default:
                return this.card;
        }
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
                ", card='" + card + '\'' +
                ", square='" + square + '\'' +
                ", original='" + original + '\'' +
                ", event=" + event +
                ", user=" + user +
                '}';
    }

    public String getTimeCreated() {
        return Util.millisTimestampToPrettyTime(created);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
