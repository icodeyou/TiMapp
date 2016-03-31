package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.utils.Util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 2/2/2016.
 */
public class ServerConfiguration {

    private static final String TAG = "ServerConfiguration";
    public int updated = 0;
    public int version = 0;
    public int update_configuration_delay = 3600;
    public LinkedList<Category> categories;

    public Rules rules;

    public ServerConfiguration() {
        this.updated = Util.getCurrentTimeSec();
        this.categories = new LinkedList<>();
        this.rules = new Rules();
    }

    @Override
    public String toString() {
        return "ServerConfiguration{" +
                "updated=" + updated +
                ", version=" + version +
                ", rules= " + rules.toString() +
                ", categories= " + categories.toString() +
                '}';
    }

    public class Rules {

        public int max_invite_per_request = 20;
        public int picture_max_size;
        public int picture_max_width;
        public int picture_max_height;

        public Rules() {
            this.places_points_levels = new LinkedList<>();
        }

        public List<Integer> places_points_levels;
        public int place_max_reachable = 500;
        public int tags_suggest_limit = 40;
        public int places_populars_limit = 20;
        public int places_min_delay_add = 60;
        public int places_users_min_delay_add  = 60;
        public int posts_min_tag_number = 3;
        public int posts_max_tags_number = 3;
        public int tags_min_name_length = 2;
        public int tags_max_name_length = 30;
        public String tags_name_regex = "";
        public int gps_min_time_delay = 60000;
        public int gps_min_accuracy_add_place = 3500;
        public int gps_min_accuracy = 3500;
        public int places_min_name_length = 3;
        public int tags_min_search_length = 0;

        public String toString(){
            return "Rules{" +
                    ", places_points_levels=" + places_points_levels +
                    ", place_max_reachable=" + place_max_reachable +
                    ", tags_suggest_limit=" + tags_suggest_limit +
                    ", places_populars_limit=" + places_populars_limit +
                    ", places_min_delay_add=" + places_min_delay_add +
                    ", places_users_min_delay_add=" + places_users_min_delay_add +
                    ", posts_min_tag_number=" + posts_min_tag_number +
                    ", posts_max_tags_number=" + posts_max_tags_number +
                    ", tags_min_name_length=" + tags_min_name_length +
                    ", tags_max_name_length=" + tags_max_name_length +
                    ", tags_name_regex='" + tags_name_regex + '\'' +
                    ", gps_min_time_delay=" + gps_min_time_delay +
                    ", gps_min_accuracy_add_place=" + gps_min_accuracy_add_place +
                    ", gps_min_accuracy=" + gps_min_accuracy +
                    ", places_min_name_length=" + places_min_name_length +
                    ", tags_min_search_length=" + tags_min_search_length +
                    '}';
        }
    }
}
