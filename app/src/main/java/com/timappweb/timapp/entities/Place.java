package com.timappweb.timapp.entities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Place implements Serializable, MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;

    public int id = -1;

    @SerializedName("spot")
    private Spot            spot;
    public int              spot_id;
    public String           name;
    public int              created;
    public double           latitude;
    public double           longitude;
    public int              count_posts;
    public int              category_id;
    public int              points;
    public int              loaded_time = -1;
    public List<Tag>        tags;
    public ArrayList<Post>  posts;

    public Place(){
        this.loaded_time = Util.getCurrentTimeSec();
    }

    public Place(int id, double lat, double lng, String name) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.posts = new ArrayList<>();
        this.tags = new ArrayList<>();

    }

    public Place(double lat, double lng, String name, EventCategory eventCategory) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.category_id = eventCategory.id;
        this.created = Util.getCurrentTimeSec();
    }

    public Place(Location lastLocation, String name, EventCategory eventCategory, Spot spot) {
        this.loaded_time = Util.getCurrentTimeSec();
        this.latitude = lastLocation.getLatitude();
        this.longitude = lastLocation.getLongitude();
        this.name = name;
        this.category_id = eventCategory.id;
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
        return this.id == -1;
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

    @Override
    public int getId() {
        return this.id;
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
                "id=" + id +
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
                < MyApplication.getApplicationRules().place_max_reachable;
    }

    public boolean isAround() {
        Location lastLocation = MyApplication.getLastLocation();
        if (lastLocation == null) return false;
        return this.isAround(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public static boolean isValidName(String name) {
        return name.trim().length() >= MyApplication.getApplicationRules().places_min_name_length;
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
        List<Integer> levels = MyApplication.getApplicationRules().places_points_levels;
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
}
