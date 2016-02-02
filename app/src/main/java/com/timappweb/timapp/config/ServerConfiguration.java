package com.timappweb.timapp.config;

import android.util.Log;

import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by stephane on 2/2/2016.
 */
public class ServerConfiguration {

    private static final String TAG = "ServerConfiguration";
    public int updated = 0;
    public int version = 0;

    public List<Integer> place_levels;
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
    public int update_configuration_delay = 3600;

    public ServerConfiguration() {
        this.place_levels = new LinkedList<>();
        this.place_levels.add(2000);
        this.place_levels.add(4000);
        this.place_levels.add(5000);
        this.place_levels.add(7000);
        this.place_levels.add(7100);
        this.updated = Util.getCurrentTimeSec();
    }

    @Override
    public String toString() {
        return "ServerConfiguration{" +
                "updated=" + updated +
                ", version=" + version +
                ", place_levels=" + place_levels +
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
                ", update_configuration_delay=" + update_configuration_delay +
                '}';
    }
}
