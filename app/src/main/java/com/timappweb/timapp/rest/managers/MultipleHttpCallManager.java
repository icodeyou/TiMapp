package com.timappweb.timapp.rest.managers;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by Stephane on 08/08/2016.
 */
public class MultipleHttpCallManager {

    private HashMap<String, HttpCallManager> callsManager = new HashMap<>();
    private Callback callback;


    public HttpCallManager addCall(String id, HttpCallManager manager) {
        if (manager != null) this.callsManager.put(id, manager);
        return manager;
    }
    public HttpCallManager addCall(String id, Call call) {
        HttpCallManager manager = new HttpCallManager(call);
        this.callsManager.put(id, manager);
        return manager;
    }

    public void perform() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()){
                    try {
                        if (!entry.getValue().getCall().isExecuted())
                            entry.getValue().execute();
                    } catch (IOException e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (MultipleHttpCallManager.this.callback != null) {
                    MultipleHttpCallManager.this.callback.onPostExecute();
                    if (MultipleHttpCallManager.this.isAllSuccess()) {
                        MultipleHttpCallManager.this.callback.onAllSuccess();
                    } else {
                        MultipleHttpCallManager.this.callback.onSomeFailure();
                    }
                }
            }

        }.execute();
    }

    public MultipleHttpCallManager setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public int countSuccess(){
        int count = 0;
        for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()){
            HttpCallManager callManager = entry.getValue();
            if (!callManager.hasError()){
                if (callManager.hasResponse() && callManager.getResponse().isSuccessful())
                    count++;
            }
        }
        return count;
    }
    public int countFailure(){
        int count = 0;
        for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()){
            HttpCallManager callManager = entry.getValue();
            if (callManager.hasError()){
                count++;
            }
        }
        return count;
    }
    public int countNotSuccessful(){
        int count = 0;
        for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()){
            HttpCallManager callManager = entry.getValue();
            if (!callManager.hasError()){
                if (callManager.hasResponse() && !callManager.getResponse().isSuccessful())
                    count++;
            }
        }
        return count;
    }
    public boolean isAllSuccess(){
        return this.countSuccess() == this.callsManager.size();
    }
    public boolean hasNoFailure(){
        return this.countFailure() == 0;
    }

    public HttpCallManager getManager(int location) {
        return callsManager.get(location);
    }

    public boolean isSuccess(String id) {
        HttpCallManager manager = callsManager.get(id);
        if (manager == null) return true;
        return !manager.hasError() && manager.hasResponse() && manager.getResponse().isSuccessful();
    }

    // --------

    public static class Callback{

        public void onPostExecute() {};

        public void onAllSuccess() {};

        public void onSomeFailure() {};
    }
}
