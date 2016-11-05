package com.timappweb.timapp.data.models;

import android.databinding.Bindable;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import java.util.List;

@Table(database = AppDatabase.class)
public class Spot extends SyncBaseModel implements ClusterItem {

    // TODO
    public enum StatusEnum {VALIDATED, PENDING, BLACKLIST}

    // =============================================================================================
    // DATABASE

    @Column
    @Expose
    @SerializedName("name")
    public String name;

    @Column
    @Expose
    @SerializedName("description")
    public String description;

    @Column
    @Expose
    @SerializedName("latitude")
    public double latitude;

    @Column
    @Expose
    @SerializedName("longitude")
    public double longitude;

    @ForeignKey(
            onDelete = ForeignKeyAction.SET_NULL,
            onUpdate = ForeignKeyAction.CASCADE,
            saveForeignKeyModel = false,
            stubbedRelationship = false
    )
    @SerializedName("spot_category")
    @Expose
    public SpotCategory category;
    /*
    @Column(name = "Status")
    @Expose
    @SerializedName("status")
    public StatusEnum status;*/

    // =============================================================================================
    // Fields

    /*
    @SerializedName("tags")
    @Expose(deserialize = true, serialize = true)
    public List<Tag> tags;
    */


    // =============================================================================================

    public String address;

    // =============================================================================================

    public Spot() {}

    public Spot(String name) {
        this.name = name;
    }

    public Spot(String name, SpotCategory category) {
        this.name = name;
        this.category = category;
    }

    // =============================================================================================

    @Override
    public String toString() {
        return "Spot{" +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }


    // =============================================================================================
    // Setters/Getters

    /**
     * TODO use converter for GSON...
     * @param category
     */
    public void setCategory(SpotCategory category) {
        this.category = category;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public SpotCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasAddress(){
        return address != null;
    }

    // =============================================================================================

/*
    public void setAddress(String address) {
        this.address = address;
        notifyPropertyChanged(BR.address);
    }*/

    @Bindable
    public String getAddress() {
        return address;
    }

    public static boolean isValidName(String name) {
        return name != null && name.length() >= ConfigurationProvider.rules().spot_min_name_length;
    }

    public boolean hasCategory(SpotCategory category) {
        return (this.category != null && this.category.equals(category)) || (this.category == null && category == null);
    }
    public boolean hasCategory() {
        return this.category != null;
    }

    public boolean isNew(){
        return !this.hasRemoteId();
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public void setCategory(long id) {
        this.category = ConfigurationProvider.getSpotCategoryByRemoteId(id);
    }

    @Override
    public void deepSave() throws CannotSaveModelException {
        this.mySave();
    }

    @Override
    public int getSyncType() {
        return DataSyncAdapter.SYNC_TYPE_SPOT;
    }
}

