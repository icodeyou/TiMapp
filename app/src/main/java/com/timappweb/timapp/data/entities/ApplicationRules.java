package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 4/26/2016.
 */
public class ApplicationRules {

    @Expose
    public String tags_name_regex = "";

    @Expose
    public int max_invite_per_request = 20;

    @Expose
    public long picture_min_size;

    @Expose
    public int picture_max_size;

    @Expose
    public int picture_max_width;

    @Expose
    public int picture_max_height;

    @Expose
    public int place_max_reachable = 500;

    @Expose
    public int tags_suggest_limit = 40;

    @Expose
    public int places_populars_limit = 20;


    @Expose
    public int posts_min_tag_number = 3;

    @Expose
    public int posts_max_tags_number = 3;

    @Expose
    public int tags_min_name_length = 2;

    @Expose
    public int tags_max_name_length = 30;

    @Expose
    public int gps_min_time_delay = 60000;

    @Expose
    public int gps_min_accuracy_add_place = 3500;

    @Expose
    public int gps_min_accuracy = 3500;

    @Expose
    public int places_min_delay_add = 60; // TODO use... or remove if redondant with quota...

    @Expose
    public int places_min_name_length = 3;

    @Expose
    public int places_max_name_length = 0; // @warning used to check if configuration is correctly loaded. Do not change default

    @Expose
    public int spots_min_name_length = 3; // TODO use

    @Expose
    public int spots_max_name_length = 30;

    @Expose
    public int tags_min_search_length = 0;

    @Expose
    public List<Integer> places_points_levels = new LinkedList<>();

    @Expose
    public int spot_min_name_length = 2;

    public String toString(){
        return "ApplicationRules{" +
                ", places_points_levels=" + places_points_levels +
                ", place_max_reachable=" + place_max_reachable +
                ", tags_suggest_limit=" + tags_suggest_limit +
                ", places_populars_limit=" + places_populars_limit +
                ", places_min_delay_add=" + places_min_delay_add +
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
                ", spot_min_name_length=" + spot_min_name_length +
                '}';
    }
}
