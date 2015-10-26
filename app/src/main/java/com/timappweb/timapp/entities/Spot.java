package com.timappweb.timapp.entities;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.timappweb.timapp.data.LocalPersistenceManager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by stephane on 8/20/2015.
 */
public class Spot implements ClusterItem, Serializable {


    private static final String TAG = "EntitySpot";

    public int id;
    public int user_id;
    public double latitude;
    public double longitude;
    protected int created;
    protected int expired;
    public String tag_string;
    public String message;

    public List<Tag> tags;


    public Spot(){

    }

    public Spot(LatLng ll) {
        this.latitude = ll.latitude;
        this.longitude = ll.longitude;
    }

    public Spot(int id, int user_id, double latitude, double longitude, int created, String tag_string) {
        this.id = id;
        this.user_id = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.created = created;
        this.tag_string = tag_string;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }


    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_CREATED = "created";
    private static final String KEY_EXPIRED = "expired";
    private static final String KEY_TAG = "tags";
    private static final String KEY_IS_BROADCASTING = "isBroadcasting";

    public void writeToPref() {
        Log.i(TAG, "Writing the spot in pref: " + this);
        LocalPersistenceManager.instance.editor.putFloat(KEY_LATITUDE, (float) this.latitude);
        LocalPersistenceManager.instance.editor.putFloat(KEY_LONGITUDE, (float) this.longitude);
        LocalPersistenceManager.instance.editor.putLong(KEY_CREATED, this.created);
        LocalPersistenceManager.instance.editor.putLong(KEY_EXPIRED, this.expired);
        LocalPersistenceManager.instance.editor.putString(KEY_TAG, this.tag_string);
        LocalPersistenceManager.instance.editor.putBoolean(KEY_IS_BROADCASTING, true);

        LocalPersistenceManager.instance.editor.commit();
    }

    public static Spot loadFromPref(){
        Spot spot = new Spot();
        Log.i(TAG, "Loading the spot from pref");
        spot.longitude = LocalPersistenceManager.instance.pref.getFloat(KEY_LONGITUDE, 0);
        spot.latitude = LocalPersistenceManager.instance.pref.getFloat(KEY_LATITUDE, 0);
        spot.created = LocalPersistenceManager.instance.pref.getInt(KEY_CREATED, 0);
        //spot.expired = LocalPersistenceManager.instance.pref.getLong(KEY_EXPIRED,0);
        spot.tag_string = LocalPersistenceManager.instance.pref.getString(KEY_TAG, "");

        return spot;
    }

    public boolean isExpired() {
        return expired <= new Date().getTime();
    }

    public static boolean isBroadcasting(){
        return LocalPersistenceManager.instance.pref.getBoolean(KEY_IS_BROADCASTING, false);
    }

    public static void removeFromPref() {
        Log.i(TAG, "Removing the spot from pref");
        LocalPersistenceManager.instance.editor.putBoolean(KEY_IS_BROADCASTING, false);
        LocalPersistenceManager.instance.editor.commit();
    }

    /*
    public int getRemainingTime() {
        if (this.expired == null){
            return 0;
        }
        return Math.max(0, this.expired - this.created);
    }

    public void setExpired(Long expired) {
        this.expired = expired;
    }
    */
    public void setCreated(int created) {
        this.created = created;
    }

    public int getExpired() {
        return expired;
    }
    public int getCreated() {
        return created;
    }

    public String getCreatedDate() {
        return new Date(this.created).toString();
    }
    public String getExpiredDate() {
        return new Date(this.expired).toString();
    }

    @Override
    public LatLng getPosition() {
        return this.getLatLng();
    }


    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", created=" + created +
                ", tag_string='" + tag_string + '\'' +
                '}';
    }
}
