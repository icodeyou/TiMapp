package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.Configuration;
import com.timappweb.timapp.utils.DistanceHelper;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class Place implements Serializable, MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;
    public int id = -1;
    public String name;
    public double latitude;
    public int created;
    public double longitude;
    public int count_posts;
    public int category_id;
    public List<Tag> tags;

    public ArrayList<Post> posts;

    public Place(int id, double lat, double lng, String name) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_posts = 0;
        this.posts = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public Place(double lat, double lng, String name, Category category) {
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.category_id = category.id;
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

    public int countSpot(){
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
                '}';
    }

    public int countUsers() {
        return this.count_posts;
    }

    public int getResource() {
        return MyApplication.getCategory(this.category_id).resource;
    }

    /**
     * @param latitude
     * @param longitude
     * @return true if the user can post in the place
     */
    public boolean isReachable(double latitude, double longitude) {
        return DistanceHelper.distFrom(latitude, longitude, this.latitude, this.longitude)
                < MyApplication.config.getInt(Configuration.PLACE_REACHABLE_DISTANCE, 0);
    }

    public boolean isReachable() {
        return this.isReachable(MyApplication.getLastLocation().getLatitude(), MyApplication.getLastLocation().getLongitude());
    }

    public static boolean isValidName(String name) {
        return name.trim().length() >= MyApplication.config.getInt(Configuration.PLACE_MIN_LENGTH);
    }

}
