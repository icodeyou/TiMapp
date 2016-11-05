package com.timappweb.timapp.data.models;

 import com.raizlabs.android.dbflow.annotation.Column;
 import com.raizlabs.android.dbflow.annotation.ForeignKey;
 import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
 import com.raizlabs.android.dbflow.annotation.NotNull;
 import com.raizlabs.android.dbflow.annotation.Table; import com.timappweb.timapp.data.AppDatabase; import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
 import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.Util;

@Table(database = AppDatabase.class)
public class Picture extends SyncBaseModel {

    public enum ThumbnailType{ CARD, SQUARE }

    // =============================================================================================
    // DATABASE

    /*
    @Column(name = "Photo", notNull = true)
    @Expose(serialize = true, deserialize = true)
    public String photo;*/
    @Column(name = "OriginalUrl")
    @Expose(serialize = true, deserialize = true)
    public String original;

    @Column(name = "Preview")
    @Expose(serialize = true, deserialize = true)
    @SerializedName("thumbnail_card")
    public String card;

    @Column(name = "Square")
    @Expose(serialize = true, deserialize = true)
    @SerializedName("thumbnail_square")
    public String square;

    /*
    @Column(name = "PhotoDir", notNull = true)
    @Expose(serialize = true, deserialize = true)
    public String photo_dir;*/

    @ModelAssociation(joinModel = Event.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = Event.class,
            stubbedRelationship = true,
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE)
    @NotNull
    @SerializedName("place")
    @Expose(serialize = true, deserialize = true)
    public Event event;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = User.class,
            saveForeignKeyModel = true,
            stubbedRelationship = true,
            onDelete = ForeignKeyAction.SET_NULL,
            onUpdate = ForeignKeyAction.CASCADE)
    @Expose(serialize = true, deserialize = true)
    public User user;

    /*
    @Column(name = "BaseUrl")
    @Expose(serialize = true, deserialize = true)
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
    public int getSyncType() {
        throw new InternalError("Not syncable");
    }

    @Override
    public String toString() {
        return "Picture{" +
                ", id=" + id +
                ", created=" + created +
                ", card='" + card + '\'' +
                ", square='" + square + '\'' +
                ", original='" + original + '\'' +
                ", event=" + event +
                ", user=" + user +
                '}';
    }

    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(created);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void deepSave() throws CannotSaveModelException {
        if (this.user != null) this.user.deepSave();
        if (this.event != null) event.deepSave();
        this.mySave();
    }
}
