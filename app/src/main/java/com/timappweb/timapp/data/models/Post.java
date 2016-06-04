package com.timappweb.timapp.data.models;

import android.location.Location;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Post")
public class Post extends SyncBaseModel implements Serializable, MarkerValueInterface, PlaceUserInterface {

    private static final String TAG = "EntitySpot";

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User")
    @Expose
    public User user;

    @Column(name = "Latitude")
    @Expose
    public double latitude;

    @Column(name = "Longitude")
    @Expose
    public double longitude;

    @Column(name = "Created")
    @Expose
    public int created;

    @Column(name = "Comment")
    @Expose
    public String comment;

    @Column(name = "Anonymous")
    @Expose
    public boolean anonymous;

    public Event event;

    // =============================================================================================
    // FIELDS

    @Expose
    public int place_id;

    @Expose(deserialize = false, serialize = true)
    public String tag_string;

    @Expose
    private List<Tag> tags;

    // =============================================================================================
    // GETTERS

    // =============================================================================================

    public String country;
    public String state;
    public String city;
    public String route;
    public String street_number;
    public String address = "";

    public Post(Event events) {
        this.event = event;
    }


    public void setTags(List<Tag> tags){
        this.tag_string = "";
        if (tags.size() == 0){
            this.tags = tags;
            return;
        }
        for (int i = 0; i < tags.size() - 1; i++){
            this.tag_string += tags.get(i).name + ",";
        }
        this.tag_string += tags.get(tags.size() -1).name;
        this.tags = tags;
    }

    public Post() {
        this.tags = new ArrayList<>();
    }

    public Post(LatLng ll) {
        this.latitude = ll.latitude;
        this.longitude = ll.longitude;
        this.tags = new ArrayList<>();
    }

    public Post(int id, double latitude, double longitude, int created, String tag_string) {
        this.remote_id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.created = created;
        this.tag_string = tag_string;
        this.tags = new ArrayList<>();
    }

    private static int dummyIndice = 1;

    public static Post createDummy() {
        Post p = new Post(dummyIndice++, dummyIndice, dummyIndice, 0, "");
        p.comment = "C'est le post numero: " + dummyIndice;
        p.tags.add(new Tag("Carrot", 0));
        p.tags.add(new Tag("Snow", 0));
        p.tags.add(new Tag("Choux", 0));
        p.tags.add(new Tag("Fleur", 0));
        p.tags.add(new Tag("Sous", 0));
        p.tags.add(new Tag("marin", 0));
        p.tags.add(new Tag("CrOTTe", 0));

        p.user = User.createDummy();

        p.address = "31 rue de la brouettitude 47584 Charrue les bois";
        return p;
    }

    public LatLng getLocation() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLocation(Location location){
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }


    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        /*
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        Log.d(TAG, "GMT offset is " + (mGMTOffset / 1000) + " seconds for time zone " + mTimeZone.getDisplayName());
        return p.format(new Date(((long)this.created)* 1000 + mGMTOffset));
        */
        // TODO [TEST] diffent time zone on phone
        return Util.secondsTimestampToPrettyTime(this.created);
    }


    @Override
    public LatLng getPosition() {
        return this.getLocation();
    }


    @Override
    public String toString() {
        return "Post{" +
                "id=" + remote_id +
                ", place_id=" + place_id +
                ", user=" + user +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", created=" + created +
                ", tag_number=" + (tags != null ? tags.size(): 0) +
                ", tag_string='" + tag_string + '\'' +
                '}';
    }

    public ArrayList getTagsToStringArray() {
        ArrayList<String> res = new ArrayList<String>();
        for (Tag tag : this.tags) {
            res.add(tag.getName());
        }
        return res;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String getTimeCreated() {
        return getPrettyTimeCreated();
    }


    @Override
    public int getMarkerId() {
        return this.remote_id;
    }

    public boolean validateForSubmit() {
        if (this.tag_string.length() == 0) {
            //mTvComment.setError("You must select at least one tag");
            return false;
        }
        return true;
    }

    public String getAddress() {
        if (country != null) {
            if (city != null) {
                if (route != null) {
                    address += route + ", ";
                }
                address += city;
            }
            address += " (" + country + ")";
        }
        return address;
    }

    public String getUsername() {
        return user != null
                ? (anonymous ? "Anonymous" : user.username)
                : "Former user";
    }


    public User getUser() {
        return user;
    }

    public boolean hasTagsLoaded() {
        return tags != null && tags.size() > 0;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }
}