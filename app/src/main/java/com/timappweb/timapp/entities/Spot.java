package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by stephane on 12/2/2015.
 */
public class Spot implements ClusterItem, Serializable {

    public int id;
    public String name;
    public double latitude;
    public double longitude;
    public int count_post;
    public ArrayList<Post> posts;

    public Spot(int id, double lat, double lng, String name) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.name = name;
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

    public static Spot createDummy(){
        Spot spot = new Spot(1, dummyIndice, dummyIndice, "Test");
        spot.addPost(Post.createDummy());
        spot.addPost(Post.createDummy());
        spot.addPost(Post.createDummy());
        spot.addPost(Post.createDummy());
        dummyIndice++;
        return spot;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(this.latitude, this.longitude);
    }
}
