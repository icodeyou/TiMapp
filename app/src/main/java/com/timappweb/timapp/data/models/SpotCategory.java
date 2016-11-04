package com.timappweb.timapp.data.models;

 import com.raizlabs.android.dbflow.annotation.Column;
 import com.raizlabs.android.dbflow.annotation.Table; import com.timappweb.timapp.data.AppDatabase;
 import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(database = AppDatabase.class)
public class SpotCategory extends Category{

    // =============================================================================================

    @Expose
    @SerializedName("name")
    @Column
    public String name;

    @Expose
    @SerializedName("position")
    @Column
    public int position;

    @Expose
    @SerializedName("icon")
    @Column(name = "ResourceName")
    public String iconUrl;

    // =============================================================================================

    public SpotCategory() {
        super();
    }

    public SpotCategory(String name) {
        super();
        this.name = name;
    }

    // =============================================================================================
    // SyncBaseModel overides

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof SpotCategory)) return false;
        SpotCategory that = (SpotCategory) model;

        if (position != that.position) return false;
        return name.equals(that.name);
    }

    // =============================================================================================

    @Override
    public String toString() {
        return "SpotCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", position=" + position +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SpotCategory that = (SpotCategory) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int getSyncType() {
        throw new InternalError("Not syncable");
    }


    public String getIconLocalFilename() {
        return "ic_spot_category_"+ this.getRemoteId();
    }

    @Override
    public String getIconUrl() {
        return this.iconUrl;
    }
}
