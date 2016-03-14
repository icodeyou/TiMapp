package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable, MarkerValueInterface {

    private static final String TAG = "EntitySpot";

    public int id;
    public User user;
    public double latitude;
    public double longitude;
    public int place_id;
    public int created;
    public String tag_string;
    public String comment;
    public boolean anonymous;
    private List<Tag> tags;

    public String country;
    public String state;
    public String city;
    public String route;
    public String street_number;

    public String address = "";


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
        this.id = id;
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

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }


    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_CREATED = "created";
    private static final String KEY_TAG = "tags";
    private static final String KEY_IS_BROADCASTING = "isBroadcasting";

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
        PrettyTime p = new PrettyTime();
        // TODO [TEST] diffent time zone on phone
        return p.format(new Date(((long) this.created) * 1000));
    }


    @Override
    public LatLng getPosition() {
        return this.getLatLng();
    }


    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
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
    public int getId() {
        return this.id;
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
}