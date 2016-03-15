package com.timappweb.timapp.entities;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;

/**
 * Created by stephane on 2/18/2016.
 */
public class Picture {

    public int id;
    public String url;
    public String photo;
    public String photo_dir;
    public int place_id;
    public int user_id;

    public Picture() {
        this.url = MyApplication.getCurrentUser().getProfilePictureUrl();
    }

    public String getUrl(){
        return this.photo_dir + this.photo;
    }
}
