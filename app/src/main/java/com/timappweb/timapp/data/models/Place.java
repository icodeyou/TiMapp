package com.timappweb.timapp.data.models;

import android.location.Location;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "Place")
public class Place extends SyncBaseModel implements Serializable, MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;


    @SerializedName("spot")
    @Expose
    public Spot spot;

    @Column(name = "SpotId")
    @Expose
    public int              spot_id;

    @Column(name = "Name")
    @Expose
    public String           name;

    @Column(name = "Distance")
    @Expose
    public double           distance = -1;


    @Column(name = "Description")
    @Expose
    public String           description;

    @Column(name = "Created")
    @Expose
    public int              created;

    @Column(name = "Latitude")
    @Expose
    public double           latitude;

    @Column(name = "Longitude")
    @Expose
    public double           longitude;

    @Expose(serialize = false, deserialize = true)
    public int              count_posts;

    @Column(name = "CategoryId")
    @Expose
    public int              category_id;

    @Column(name = "Points")
    @Expose(serialize = false, deserialize = true)
    public int              points;

    @Expose(serialize = false, deserialize = false)
    public int              loaded_time = -1;

    @Expose(serialize = false, deserialize = true)
    public List<Tag>        tags;

    @Expose(serialize = false, deserialize = true)
    public ArrayList<Post>  posts;

    public Place(){
        this.loaded_time = Util.getCurrentTimeSec();
        setDistancePlace();
    }

    public Place(int id, double lat, double lng, String name) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.remote_id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.posts = new ArrayList<>();
        this.tags = new ArrayList<>();
        setDistancePlace();
    }

    /*public Place(double lat, double lng, String name, EventCategory eventCategory) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.category_id = eventCategory.remote_id;
        this.created = Util.getCurrentTimeSec();
    }*/

    public Place(Location lastLocation, String name, EventCategory eventCategory, Spot spot, String description) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.latitude = lastLocation.getLatitude();
        this.longitude = lastLocation.getLongitude();
        this.name = name;
        this.category_id = eventCategory.remote_id;
        this.description = description;
        setDistancePlace();
        if (spot != null){
            this.spot = spot;
            this.spot_id = spot.id;
        }
    }

    public void addPost(Post post){
        this.count_posts++;
        this.posts.add(post);
    }

    /**
     * Return true if this place is not saved on the server
     * @return
     */
    public boolean isNew(){
        return this.remote_id == -1;
    }

    public int countPosts(){
        return this.count_posts;
    }

    // Dummy data
    private static int dummyIndice = 0;

    public static Place createDummy(){
        Place place = new Place(1, dummyIndice, dummyIndice, "Test");
        place.tags.add(Tag.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        place.addPost(Post.createDummy());
        dummyIndice++;
        return place;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(this.latitude, this.longitude);
    }


    public String getTime() {
        if (this.posts != null && posts.size() > 0){
            return posts.get(posts.size()-1).getPrettyTimeCreated();
        }
        return this.getPrettyTimeCreated();
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(((long) this.created) * 1000));
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + remote_id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", created=" + created +
                ", longitude=" + longitude +
                ", category_id=" + category_id +
                ", spot=" + (spot != null ? spot : "No spot") +
                '}';
    }

    public int countUsers() {
        return this.count_posts;
    }

    /**
     * @param latitude
     * @param longitude
     * @return true if the user can post in the place
     */
    public boolean isAround(double latitude, double longitude) {
        return DistanceHelper.distFrom(latitude, longitude, this.latitude, this.longitude)
                < ConfigurationProvider.rules().place_max_reachable;
    }

    public boolean isAround() {
        Location lastLocation = MyApplication.getLastLocation();
        if (lastLocation == null) return false;
        return this.isAround(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public static boolean isValidName(String name) {
        return name.trim().length() >= ConfigurationProvider.rules().places_min_name_length;
    }

    public int getPoints() {
        int points = this.points - (Util.getCurrentTimeSec() - this.loaded_time);
        return points > 0 ? points : 0;
    }

    public int getCategoryId() {
        return category_id;
    }

    public int getLevel(){
        return Place.computeLevel(this.getPoints());
    }

    private static int computeLevel(int points) {
        List<Integer> levels = ConfigurationProvider.rules().places_points_levels;
        int num = 0;
        for (int level: levels){
            if (level >= points){
                return num;
            }
            num++;
        }
        return num;
    }

    public EventCategory getCategory() throws UnknownCategoryException {
        return MyApplication.getCategoryById(this.category_id);
    }


    @Override
    public int getMarkerId() {
        return this.remote_id;
    }

    // =============================================================================================

    @Override
    public boolean isSync(SyncBaseModel o) {
        if (o == null) return false;
        Place place = (Place) o;

        if (spot_id != place.spot_id) return false;
        if (created != place.created) return false;
        if (Double.compare(place.latitude, latitude) != 0) return false;
        if (Double.compare(place.longitude, longitude) != 0) return false;
        if (count_posts != place.count_posts) return false;
        if (category_id != place.category_id) return false;
        if (points != place.points) return false;
        if (!name.equals(place.name)) return false;
        return !(description != null ? !description.equals(place.description) : place.description != null);
    }



    // =============================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Place place = (Place) o;

        if (remote_id != place.remote_id) return false;
        if (spot_id != place.spot_id) return false;
        if (created != place.created) return false;
        if (Double.compare(place.latitude, latitude) != 0) return false;
        if (Double.compare(place.longitude, longitude) != 0) return false;
        if (count_posts != place.count_posts) return false;
        if (category_id != place.category_id) return false;
        if (points != place.points) return false;
        if (spot != null ? !spot.equals(place.spot) : place.spot != null) return false;
        if (!name.equals(place.name)) return false;
        return !(description != null ? !description.equals(place.description) : place.description != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + remote_id;
        return result;
    }

    public void setDistancePlace() {
        Location location = MyApplication.getLastLocation();
        double userLongitude = location.getLongitude();
        double userLatitude = location.getLatitude();
        double distance =  DistanceHelper.distFrom(userLatitude, userLongitude,
                this.latitude, this.longitude);
        this.distance = Math.round(distance);
    }

}
