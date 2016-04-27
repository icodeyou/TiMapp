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

    private static final int CONFIG_ID_RULES = 1;

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

    /*
    public ConfigurationProvider(Context context, Listener listener) {
        this.context = context;
        this.sharedPref = context.getSharedPreferences(PREF_NAME, SHARED_PREF_PRIVATE_MODE);
        this.listener = listener;

        this.init();
    }

    private SyncConfigManager buildConfManager(int id, String path){
        return new SyncConfigManager(
                id,
                new RESTRemoteSync(path, RestClient.instance().createService(ConfigInterface.class)),
                new SharedPrefSync("config_" + id, sharedPref));
    }

    private void init(){
        //eventCatagoriesManager = buildConfManager(CONFIG_ID_EVENT_CATEGORIES, "event_categories");
        //spotCatagoriesManager = buildConfManager(CONFIG_ID_SPOT_CATEGORIES, "spot_categories");
        //rulesManager = buildConfManager(CONFIG_ID_RULES, "rules");
    }

    public AsyncTask<Integer, Integer, Boolean> load() {
        Log.d(TAG, "Start sync configuration from server...");
        AsyncTask<Integer, Integer, Boolean> loadTask = new AsyncTask<Integer, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Integer... params) {
                try {
                    //eventCatagoriesManager.sync();
                    //spotCatagoriesManager.sync();
                    //rulesManager.sync();
                } catch (RemotePersistenceManager.CannotLoadException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (!success){
                    listener.onFail();
                }
                else{
                    listener.onLoaded();
                }
            }
        };
        return loadTask.execute();
    }

    public void clear() {
        //eventCatagoriesManager.clear();
        //spotCatagoriesManager.clear();
        rulesManager.clear();
    }

    public interface Listener{
        void onLoaded();
        void onFail();
    }

    @Override
    public String toString() {
        return "ServerConfiguration{" +
                ", rules=" + rulesManager +
                //", event categories= " + eventCatagoriesManager.toString() +
                //", spot categories= " + spotCatagoriesManager.toString() +
                '}';
    }
*/
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
