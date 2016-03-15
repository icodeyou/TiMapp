package com.timappweb.timapp.entities;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;

public class Picture {

    public int id;
    public String url;
    public String photo;
    public String photo_dir;
    public int place_id;
    public int user_id;

    public Picture() {

    }

    public Picture(User user) {
        //this.url = user.getProfilePictureUrl();
        this.url = "https://graph.facebook.com/448481685342362/picture?type=large";
    }

    public String getUrl(){
        return this.photo_dir + this.photo;
    }
}
