package com.timappweb.timapp.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;

import java.util.List;

@Table(database = AppDatabase.class)
public class EventCategory extends Category{

    private static final String TAG = "EventCategory";

    // =============================================================================================

    @Expose(serialize = false, deserialize = true)
    @Column
    public String name;

    @Expose(serialize = false, deserialize = true)
    @Column
    @SerializedName("icon")
    public String iconUrl;

    @Expose(serialize = false, deserialize = true)
    @Column
    public int position;


    // =============================================================================================


    public EventCategory() {
    }

    public EventCategory(String name) {
        this.name = name;
        this.position = 0;
    }
    public EventCategory(String name, int position) {
        this.name = name;
        this.position = position;
    }

    // =============================================================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static String idsToString(List<EventCategory> categories) {
        if (categories == null || categories.size() == 0){
            return "";
        }
        String res = String.valueOf(categories.get(0).id);

        for (int i = 1; i < categories.size(); i++){
            res += "," + String.valueOf(categories.get(i).id);
        }
        return res;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof EventCategory)) return false;
        EventCategory that = (EventCategory) model;

        if (position != that.position) return false;
        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "EventCategory{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCategory)) return false;
        if (!super.equals(o)) return false;

        EventCategory that = (EventCategory) o;

        if (id != that.id) return false;
        if (position != that.position) return false;
        return name.equals(that.name);

    }

    @Override
    public int getSyncType() {
        //return DataSyncAdapter.SYNC_TYPE_
        throw new InternalError("Not syncable element");
    }

    @Override
    public String getIconLocalFilename() {
        return "ic_event_category_"+ this.getRemoteId();
    }

    @Override
    public String getIconUrl() {
        return this.iconUrl;
    }

    @Override
    public void deepSave() throws CannotSaveModelException {
        this.mySave();
    }
}
