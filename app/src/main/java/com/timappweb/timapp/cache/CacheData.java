package com.timappweb.timapp.cache;

import android.util.Log;

import com.timappweb.timapp.config.LocalPersistenceManager;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.SearchFilter;
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

/**
 * Created by stephane on 1/29/2016.
 */
public class CacheData {

    private static final String KEY_LAST_PLACE = "cachedata_lastplace";
    private static final String KEY_LAST_POST = "cachedata_lastpost";
    private static final String KEY_MAP_USER_PLACES = "cachedata_map_user_places";
    private static final String TAG = "CacheData";
    private static final String KEY_LAST_PICTURE = "cachedata_lastpicture";;

    public static HashMap<Integer, UserPlace> mapPlaceStatus = new HashMap<>();
    private static Place lastPlace = null;
    private static Post lastPost = null;
    private static Picture lastPicture = null;
    private static SearchFilter searchFilter = null;


    public static void load(){
        Log.d(TAG, "Loading configuration from shared pref");
        lastPlace = LocalPersistenceManager.readObject(CacheData.KEY_LAST_PLACE, Place.class);
        lastPost = LocalPersistenceManager.readObject(CacheData.KEY_LAST_POST, Post.class);

        //This line is used to initialize mapPlaceStatus
        //LocalPersistenceManager.writeObject(KEY_MAP_USER_PLACES, null);

        try{
            mapPlaceStatus = LocalPersistenceManager.readObject(CacheData.KEY_MAP_USER_PLACES, HashMap.class);
        } catch (com.google.gson.JsonSyntaxException ex){
            // TODO remove oject ?
            Log.d(TAG, "Cannot read configuration from cache: " + ex.toString());
        }

        if (mapPlaceStatus == null){
            mapPlaceStatus = new HashMap<>();
        }
    }

    public static void setLastPlace(Place lastPlace) {
        CacheData.lastPlace = lastPlace;
        LocalPersistenceManager.writeObject(CacheData.KEY_LAST_PLACE, lastPlace);
        Log.d(TAG, "Setting last place: " + lastPlace);
    }

    public static void setLastPost(Post lastPost) {
        CacheData.lastPost = lastPost;
        LocalPersistenceManager.writeObject(CacheData.KEY_LAST_POST, lastPost);
        Log.d(TAG, "Setting last post: " + lastPost);
    }
    public static void setLastPicture(Picture picture) {
        CacheData.lastPicture = picture;
        LocalPersistenceManager.writeObject(CacheData.KEY_LAST_PICTURE, picture);
        Log.d(TAG, "Setting last picture: " + picture);
    }

    public static void addUserStatus(int placeId, UserPlaceStatus status){
        // TODO remove old ones
        UserPlace userPlace = new UserPlace();
        userPlace.place_id = placeId;
        userPlace.status = status;
        mapPlaceStatus.put(placeId, userPlace);
        LocalPersistenceManager.writeObject(KEY_MAP_USER_PLACES, mapPlaceStatus);
        Log.d(TAG, "Setting new user status for place " + placeId + " : " + userPlace);
    }

    // Last post
    public static boolean isAllowedToAddPlace(){
        return lastPlace == null || Util.isOlderThan(lastPlace.created, 60 ); // TODO server config
    }
    public static boolean isAllowedToAddPost(){
        return lastPost == null || Util.isOlderThan(lastPost.created, 60 );
    }
    // Last place status
    public static boolean isAllowedToAddUserStatus(int placeId, UserPlaceStatus status){
        if (mapPlaceStatus.containsKey(placeId)){
            UserPlace placeStatus = mapPlaceStatus.get(placeId);
            return placeStatus.status != status;
        }
        return true;
    }
    public static boolean isUserComing(int placeId) {
        if (mapPlaceStatus.containsKey(placeId)){
            UserPlace placeStatus = mapPlaceStatus.get(placeId);
            // If there is already a user status
            return placeStatus.status == UserPlaceStatus.COMING;
        }
        return false;
    }


    public static boolean isAllowedToAddPicture() {
        return lastPicture == null || Util.isOlderThan(lastPicture.created, 60 );
    }

}
