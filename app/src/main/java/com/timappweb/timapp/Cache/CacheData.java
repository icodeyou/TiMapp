package com.timappweb.timapp.Cache;

import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

/**
 * Created by stephane on 1/29/2016.
 */
public class CacheData {

    public static HashMap<Integer, PlaceStatus> mapPlaceStatus;
    private static Place lastPlace = null;
    private static Post lastPost = null;

    public static void setLastPlace(Place lastPlace) {
        CacheData.lastPlace = lastPlace;
    }

    public static void setLastPost(Post lastPost) {
        CacheData.lastPost = lastPost;
    }

    // Last post
    public static boolean isAllowedToAddPlace(){
        return lastPlace == null || Util.isOlderThan(lastPlace.created, 60 * 1000);
    }
    public static boolean isAllowedToAddPost(){
        return lastPost == null || Util.isOlderThan(lastPost.created, 60 * 1000);
    }
    // Last place status
    public static boolean isAllowedToAddUserStatus(int placeId, UserPlaceStatus status){
        if (mapPlaceStatus.containsKey(placeId)){
            PlaceStatus placeStatus = mapPlaceStatus.get(placeId);
            // If there is already a user status
            if (placeStatus.status == status && !Util.isOlderThan(placeStatus.created, 60* 1000) ){
                return true;
            }
        }
        return false;
    }


    private class PlaceStatus {
        public UserPlaceStatus status;
        public int created;
    }

}
