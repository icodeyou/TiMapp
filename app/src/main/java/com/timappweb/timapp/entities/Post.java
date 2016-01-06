package com.timappweb.timapp.entities;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.timappweb.timapp.data.LocalPersistenceManager;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Post implements Serializable, MarkerValueInterface {

    private static final String TAG = "EntitySpot";

    public int id;
    public User user;
    public double latitude;
    public double longitude;
    protected int created;
    public String tag_string;
    public String comment;
    public boolean anonymous;

    public String country;
    public String state;
    public String city;
    public String route;
    public String street_number;

    public String address = "";

    public ArrayList<Tag> tags;


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

    public void writeToPref() {
        Log.i(TAG, "Writing the spot in pref: " + this);
        LocalPersistenceManager.instance.editor.putFloat(KEY_LATITUDE, (float) this.latitude);
        LocalPersistenceManager.instance.editor.putFloat(KEY_LONGITUDE, (float) this.longitude);
        LocalPersistenceManager.instance.editor.putLong(KEY_CREATED, this.created);
        LocalPersistenceManager.instance.editor.putString(KEY_TAG, this.tag_string);
        LocalPersistenceManager.instance.editor.putBoolean(KEY_IS_BROADCASTING, true);

        LocalPersistenceManager.instance.editor.commit();
    }

    public static Post loadFromPref() {
        Post spot = new Post();
        Log.i(TAG, "Loading the spot from pref");
        spot.longitude = LocalPersistenceManager.instance.pref.getFloat(KEY_LONGITUDE, 0);
        spot.latitude = LocalPersistenceManager.instance.pref.getFloat(KEY_LATITUDE, 0);
        spot.created = LocalPersistenceManager.instance.pref.getInt(KEY_CREATED, 0);
        spot.tag_string = LocalPersistenceManager.instance.pref.getString(KEY_TAG, "");

        return spot;
    }


    public static boolean isBroadcasting() {
        return LocalPersistenceManager.instance.pref.getBoolean(KEY_IS_BROADCASTING, false);
    }

    public static void removeFromPref() {
        Log.i(TAG, "Removing the spot from pref");
        LocalPersistenceManager.instance.editor.putBoolean(KEY_IS_BROADCASTING, false);
        LocalPersistenceManager.instance.editor.commit();
    }

    public void setCreated(int created) {
        this.created = created;
    }

    /**
     * Get created date as a timestamp
     *
     * @return
     */
    public int getCreated() {
        return created;
    }

    /**
     * Get the created as a pretty time format
     *
     * @return
     */
    public String getPrettyTimeCreated() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        Log.d(TAG, "GMT offset is " + (mGMTOffset / 1000) + " seconds for time zone " + mTimeZone.getDisplayName());

        PrettyTime p = new PrettyTime();
        //return p.format(new Date(((long)this.created)* 1000 + mGMTOffset));
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

    public ArrayList<Tag> getTags() {
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

    public String getAdress() {
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

    public boolean hasTagsLoaded() {
        return tags != null && tags.size() > 0;
    }

    public String getComment() {
        return comment;
    }
}