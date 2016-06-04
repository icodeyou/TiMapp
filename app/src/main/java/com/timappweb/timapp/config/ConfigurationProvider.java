package com.timappweb.timapp.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Select;
import com.timappweb.timapp.configsync.RESTRemoteSync;
import com.timappweb.timapp.configsync.RemotePersistenceManager;
import com.timappweb.timapp.configsync.SharedPrefSync;
import com.timappweb.timapp.configsync.SyncConfigManager;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.services.ConfigInterface;
import com.timappweb.timapp.utils.KeyValueStorage;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 1/24/2016.
 */
public class ConfigurationProvider {

    private static final String TAG = "ConfigurationProvider";
    private static final String KEY_APPLICATION_RULE = "application_rules";

    private static OnConfigurationLoadedListener listener;
    private static ApplicationRules applicationRules;
    private static List<EventCategory> eventCategories = null;
    private static List<SpotCategory> spotCategories = null;

    public static List<EventCategory> eventCategories(){
        if (eventCategories == null){
            eventCategories = new Select().from(EventCategory.class).orderBy("Position ASC").execute();
        }
        return eventCategories;
    }

    public static List<SpotCategory> spotCategories(){
        if (spotCategories == null){
            spotCategories = new Select().from(SpotCategory.class).orderBy("Position ASC").execute();
        }
        return spotCategories;
    }

    public static ApplicationRules rules(){
        if (applicationRules == null){
            applicationRules = KeyValueStorage.instance.get(KEY_APPLICATION_RULE, ApplicationRules.class);
            if (applicationRules == null){
                throw new IncompleteConfigurationException("Missing application rules");
            }
        }
        return applicationRules;
    }

    public static void init(OnConfigurationLoadedListener listener){
        ConfigurationProvider.listener = listener;
    }

    /**
     *
     * @return
     */
    public static boolean hasFullConfiguration(){
        try{
            ApplicationRules rules = rules();
            return rules.places_max_name_length > 0;
        }
        catch (IncompleteConfigurationException ex){
            return false;
        }
    }

    public static void setApplicationRules(ApplicationRules applicationRules) {
        ConfigurationProvider.applicationRules = applicationRules;
        KeyValueStorage.instance.set(KEY_APPLICATION_RULE, applicationRules);
        listener.onLoaded(KEY_APPLICATION_RULE);
    }



    @Override
    public String toString() {
        return "ServerConfiguration{" +
                ", rules=" + rules() +
                //", event categories= " + eventCatagoriesManager.toString() +
                //", spot categories= " + spotCatagoriesManager.toString() +
                '}';
    }

    public static SpotCategory findSpotCategoriesByRemoteId(int remoteId) {
        for (SpotCategory category: spotCategories()){
            if (category.remote_id == remoteId){
                return category;
            }
        }
        return null;
    }

    private static class IncompleteConfigurationException extends Error {
        public IncompleteConfigurationException(String s) {
            super(s);
        }
    }

    public interface OnConfigurationLoadedListener{
        void onLoaded(String key);
        void onFail(String key);
    }
}
