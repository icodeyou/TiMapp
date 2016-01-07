package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

public class Place implements Serializable, MarkerValueInterface {

    public int id;
    public String location;
    public double latitude;
    public double longitude;
    public int count_post;
    public int category_id;

    public ArrayList<Post> posts;

    public Place(int id, double lat, double lng, String location) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.location = location;
        this.count_post = 0;
        this.posts = new ArrayList<>();
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
        return posts.get(posts.size()-1).getPrettyTimeCreated();
    }
}
