package com.timappweb.timapp.config;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.Category;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.rest.callbacks.RemoteMasterSyncHttpCallback;
import com.timappweb.timapp.rest.managers.MultipleHttpCallManager;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.utils.KeyValueStorage;

import java.util.List;

/**
 * Created by stephane on 1/24/2016.
 *
 * Manage application configuration coming from the server.
 * Request configuration via HTTP request if not present or if not up to date
 *
 */
public class ConfigurationProvider{

    private static final String TAG = "ConfigurationProvider";
    private static final String KEY_LAST_UPDATE = "configuration_last_update_time";
    private static final String KEY_APP_RULES = "configuration_rules";

    public static final String CALL_ID_APPLICATION_RULES = "app_rule";
    public static final String CALL_ID_SPOT_CATEGORIES = "spot_categories";
    public static final String CALL_ID_EVENT_CATEGORIES = "event_categories";
    private static final long UPDATE_CONF_MAX_DELAY = 6 * 3600 * 1000;

    private static HttpCallback listener;
    private static ApplicationRules applicationRules;
    private static List<EventCategory> eventCategories = null;
    private static List<SpotCategory> spotCategories = null;


    public static List<EventCategory> eventCategories(){
        if (eventCategories == null){
            eventCategories = new Select().from(EventCategory.class).orderBy("Position ASC").execute();
            if (eventCategories != null){
                initIcons(MyApplication.getApplicationBaseContext(), eventCategories);
            }
        }
        if (eventCategories == null || eventCategories.size() == 0){
            throw new IncompleteConfigurationException("Missing spot categories");
        }
        return eventCategories;
    }

    /**
     *
     * @return
     */
    public static List<SpotCategory> spotCategories(){
        if (spotCategories == null){
            spotCategories = new Select().from(SpotCategory.class).orderBy("Position ASC").execute();
            if (spotCategories != null){
                initIcons(MyApplication.getApplicationBaseContext(), spotCategories);
            }
        }
        if (spotCategories == null || spotCategories.size() == 0){
            throw new IncompleteConfigurationException("Missing spot categories");
        }
        return spotCategories;
    }

    /**
     *
     * @return
     */
    public static ApplicationRules rules(){
        if (applicationRules == null){
            applicationRules = KeyValueStorage.instance.get(KEY_APP_RULES, ApplicationRules.class);
            if (applicationRules == null){
                throw new IncompleteConfigurationException("Missing application rules");
            }
        }
        return applicationRules;
    }

    /**
     * load configuration:
     *      - if configuration is not up to date or not present it will start a SYNC immediately
     *      - Otherwise call the callback directly
     */
    public static MultipleHttpCallManager load(final Context context){

        MultipleHttpCallManager callManager = RestClient.mulipleCallsManager();

        if (!hasFullConfiguration() || !ConfigurationProvider.dataUpToDate()){
            callManager.addCall(CALL_ID_APPLICATION_RULES, RestClient.service().applicationRules())
                    .onResponse(new HttpCallback<ApplicationRules>() {
                        @Override
                        public void successful(ApplicationRules feedback) {
                            ConfigurationProvider.setApplicationRules(feedback);
                        }
                    });
            callManager.addCall(CALL_ID_SPOT_CATEGORIES, RestClient.service().spotCategories())
                    .onResponse(new RemoteMasterSyncHttpCallback<SpotCategory>(SpotCategory.class, new Select().from(SpotCategory.class)){
                        @Override
                        public void successful(List<SpotCategory> categories) {
                            super.successful(categories);
                            downloadIcons(context, categories);
                        }
                    });
            callManager.addCall(CALL_ID_EVENT_CATEGORIES, RestClient.service().eventCategories())
                    .onResponse(new RemoteMasterSyncHttpCallback<EventCategory>(EventCategory.class, new Select().from(EventCategory.class)){
                        @Override
                        public void successful(List<EventCategory> categories) {
                            super.successful(categories);
                            downloadIcons(context, categories);
                        }
                    });
        }
        else{
            Log.i(TAG, "Configuration exists and is up to date");
        }
        return callManager;
    }

    private static boolean dataUpToDate() {
        long lastUpdate = KeyValueStorage.instance.getSafeLong(KEY_LAST_UPDATE, 0);
        return System.currentTimeMillis() - lastUpdate <= UPDATE_CONF_MAX_DELAY;
    }

    public static void updateLastUpdateTime() {
        Log.d(TAG, "Updating last configuration time");
        KeyValueStorage.in().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).commit();
    }

    /**
     *
     * @return
     */
    public static boolean hasFullConfiguration(){
        return hasRulesConfig() && hasEventCategoriesConfig() && hasSpotCategoriesConfig();
    }


    public static void setApplicationRules(ApplicationRules applicationRules) {
        ConfigurationProvider.applicationRules = applicationRules;
        KeyValueStorage.instance.set(KEY_APP_RULES, applicationRules);
    }



    @Override
    public String toString() {
        return "ServerConfiguration{" +
                ", rules=" + rules() +
                //", event categories= " + eventCatagoriesManager.toString() +
                //", spot categories= " + spotCatagoriesManager.toString() +
                '}';
    }

    public static SpotCategory getSpotCategoryByRemoteId(long remoteId) {
        for (SpotCategory category: spotCategories()){
            if (category.remote_id == remoteId){
                return category;
            }
        }
        return null;
    }

    public static EventCategory getEventCategoryByRemoteId(long id) {
        for (EventCategory category: eventCategories()){
            if (category.remote_id == id){
                return category;
            }
        }
        return null;
    }


    public static boolean hasRulesConfig() {
        try{
            return rules().places_max_name_length > 0;
        }
        catch (IncompleteConfigurationException ex){
            return false;
        }
    }

    public static boolean hasSpotCategoriesConfig() {
        try{
            spotCategories();
            return true;
        }
        catch (IncompleteConfigurationException ex){
            return false;
        }
    }

    public static boolean hasEventCategoriesConfig() {
        try{
            eventCategories();
            return true;
        }
        catch (IncompleteConfigurationException ex){
            return false;
        }
    }

    public static void clearAll() {
        ConfigurationProvider.clearApplicationRules();
        ConfigurationProvider.clearSpotCategories();
        ConfigurationProvider.clearEventCategories();

        ConfigurationProvider.clearStaticVariables();
    }

    private static void clearEventCategories() {
        new Delete().from(EventCategory.class).execute();
        // TODO clear icons
    }

    private static void clearSpotCategories() {
        new Delete().from(SpotCategory.class).execute();
        // TODO clear icons
    }

    private static void clearApplicationRules() {
        KeyValueStorage.in().remove(KEY_APP_RULES).commit();
    }

    private static class IncompleteConfigurationException extends Error {
        public IncompleteConfigurationException(String s) {
            super(s);
        }
    }

    public static void clearStaticVariables(){
        eventCategories = null;
        spotCategories = null;
        applicationRules = null;
    }


    public static <T extends Category> void downloadIcons(final Context context, List<T> categories){
        for (final T category: categories){
            category.loadIconFromAPI(context);
        }
    }

    public static <T extends Category> void initIcons(final Context context, List<T> categories) {
        for (final T category : categories) {
            category.loadIconFromLocalStorage(context);
        }
    }
}
