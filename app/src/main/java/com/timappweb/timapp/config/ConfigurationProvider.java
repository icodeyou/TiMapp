package com.timappweb.timapp.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.Util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by stephane on 1/24/2016.
 */
public class ConfigurationProvider {

    private static final String TAG = "AssetsPropertyReader";

    private final String filename;
    private final Gson gson;
    private Context context;
    private static Properties properties;
    private ServerConfiguration serverConfiguration = null;
    private Listener listener;

    public ConfigurationProvider(Context context, String filename, Listener listener) {
        this.gson = new Gson();
        this.context = context;
        this.properties = new Properties();
        this.filename = filename;
        this.listener = listener;
    }

    public ServerConfiguration getServerConfiguration(){
        return serverConfiguration;
    }


    public void store(){
        properties.setProperty("server_configuration", gson.toJson(serverConfiguration));
        try {
            properties.store(new FileOutputStream(filename), null);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e.toString());
            e.printStackTrace();
        }catch (IOException e) {
            Log.d(TAG, "IOException: " + e.toString());
            e.printStackTrace();
        }
    }

    public void load(){
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(filename);
            properties.load(inputStream);

            String config = properties.getProperty("server_configuration");
            serverConfiguration = gson.fromJson(config, ServerConfiguration.class);
            updateServerConfiguration();

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setInt(String name, int value){
        properties.setProperty(name, String.valueOf(value));
    }

    public int getInt(String name){
        return this.getInt(name, 0);
    }
    public int getInt(String name, int defaultValue){
        try{
            return Integer.parseInt(properties.getProperty(name));
        }
        catch (NumberFormatException ex){
            return defaultValue;
        }
    }

    public double getDouble(String name){
        return this.getDouble(name, 0.0);
    }
    public double getDouble(String name, double defaultValue){
        try{
            return Double.parseDouble(properties.getProperty(name));
        }
        catch (NumberFormatException ex){
            return defaultValue;
        }
    }

    public void updateServerConfiguration() {
        if (serverConfiguration != null && !Util.isOlderThan(serverConfiguration.update_configuration_delay, serverConfiguration.updated)){
            Log.d(TAG, "Server configuration is up to date. Last update: " + (Util.getCurrentTimeSec() - serverConfiguration.updated) + " seconds ago.");
            this.listener.onLoaded();
            return;
        }
        if (serverConfiguration == null){
            serverConfiguration = new ServerConfiguration();
        }
        Call<ServerConfiguration> updateConfigCall = RestClient.service().configuration(serverConfiguration != null ? serverConfiguration.version : 0);

        updateConfigCall.enqueue(new Callback<ServerConfiguration>() {
            @Override
            public void onResponse(Response<ServerConfiguration> response) {
                if (response.isSuccess() && response.code() == 200){
                    serverConfiguration = response.body();
                    Log.d(TAG, "New server configuration is loaded: " + serverConfiguration);
                    store();
                }
                else{
                    Log.d(TAG, "Cannot update configuration: " + response);
                }
                listener.onLoaded();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure: Cannot update configuration: " + t.getMessage());
                listener.onFail();
            }
        });
    }

    public interface Listener{
        void onLoaded();
        void onFail();
    }
}
