package com.timappweb.timapp.data.models;

import android.databinding.Bindable;
import android.location.Location;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;
import com.timappweb.timapp.BR;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.queries.AreaQueryHelper;
import com.timappweb.timapp.utils.DistanceHelper;

import java.util.List;

@Table(name = "Spot")
public class Spot extends SyncBaseModel implements ClusterItem {

    // =============================================================================================
    // DATABASE

    @Column(name = "Name")
    @Expose
    @SerializedName("name")
    public String name;

    @Column(name = "Description")
    @Expose
    @SerializedName("description")
    public String description;

    @Column(name = "Latitude")
    @Expose
    @SerializedName("latitude")
    public double latitude;

    @Column(name = "Longitude")
    @Expose
    @SerializedName("longitude")
    public double longitude;

    @Column(name = "CategoryId")
    @Expose
    @SerializedName("spot_category_id")
    public int category_id;

    @Column(name = "Created")
    @Expose(deserialize = true, serialize = false)
    public int created;

    // =============================================================================================
    // Fields

    @SerializedName("tags")
    @Expose(deserialize = true, serialize = false)
    public List<Tag> tags;

    @SerializedName("spot_category")
    @Expose(deserialize = true, serialize = false)
    public SpotCategory category;


    // =============================================================================================

    public String address;

    // =============================================================================================

    public Spot() {}

    public Spot(String name) {
        this.name = name;
    }

    public Spot(String name, List<Tag> tags) {
        this.name = name;
        this.tags = tags;
    }

    public Spot(String name, SpotCategory category) {
        this.name = name;
        this.category = category;
        if (category != null){
            this.category_id = category.remote_id;
        }
    }

    public static Spot createDummy() {
        SpotCategory spotCategory = new SpotCategory("Dummy category");
        return new Spot("Dummy spot",spotCategory);
    }

    // =============================================================================================

    @Override
    public String toString() {
        return "Spot{" +
                " db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", name='" + name + '\'' +
                ", category_id=" + category_id +
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
        if (category != null && category.remote_id != null){
            this.category_id = category.remote_id;
        }
    }

    public int getItemPicture(){
        return R.drawable.image_bar;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    public SpotCategory getCategory() {
        if (category == null){
            if (category_id != 0){
                category = ConfigurationProvider.getSpotCategoryByRemoteId(category_id);
            }
        }
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
    public static From queryByArea(LatLngBounds bounds) {
        return new Select()
                .from(Spot.class)
                .where(AreaQueryHelper.rowInBounds(bounds));
    }

    public static List<? extends SyncBaseModel> findInArea(LatLngBounds bounds) {
        return queryByArea(bounds).execute();
    }

    public void setAddress(String address) {
        this.address = address;
        notifyPropertyChanged(BR.address);
    }

    @Bindable
    public String getAddress() {
        return address;
    }

    public boolean isValid() {
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
}

