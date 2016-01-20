package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Place implements Serializable, MarkerValueInterface {

    private static final String TAG = "PlaceEntity" ;
    public int id;
    public String name;
    public double latitude;
    public int created;
    public double longitude;
    public int count_post;
    public int category_id;
    public List<Tag> main_tags;

    public ArrayList<Post> posts;

    public Place(int id, double lat, double lng, String name) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.count_post = 0;
        this.posts = new ArrayList<>();
        this.main_tags = new ArrayList<>();
    }

    public Place(double lat, double lng, String name, Category category) {
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
        this.category_id = category.id;
    }

    public void addPost(Post post){
        this.count_post++;
        this.posts.add(post);
    }

    public int countSpot(){
        return this.count_post;
    }

    // Dummy data
    private static int dummyIndice = 0;

    public static Place createDummy(){
        Place place = new Place(1, dummyIndice, dummyIndice, "Test");
        place.main_tags.add(Tag.createDummy());
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

    public List<Tag> getMainTags() {
        return main_tags;
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
        return this.count_post;
    }
}
