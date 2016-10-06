package com.timappweb.timapp.rest.managers;

import android.os.AsyncTask;
import android.util.Log;

import com.timappweb.timapp.rest.RestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Stephane on 08/08/2016.
 */
public class MultipleHttpCallManager implements RestClient.Cancelable {

    private static final String TAG = "MultipleHttpCallManager";

    public enum ExecutionMode {PARALLEL, SINGLE}

    private HashMap<String, HttpCallManager> callsManager = new HashMap<>();
    private Callback callback;
    private ExecutionMode executionModel = ExecutionMode.PARALLEL;
    private AtomicInteger workCounter;

    public MultipleHttpCallManager setExecutionMode(ExecutionMode executionModel) {
        this.executionModel = executionModel;
        return this;
    }

    public HttpCallManager addCall(String id, HttpCallManager manager) {
        if (manager != null) {
            synchronized (this.callsManager){
                this.callsManager.put(id, manager);
            }
        }
        return manager;
    }
    public HttpCallManager addCall(String id, Call call) {
        HttpCallManager manager = new HttpCallManager(call);
        synchronized (this.callsManager){
            this.callsManager.put(id, manager);
        }
        return manager;
    }

    public void perform() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                MultipleHttpCallManager.this.execute();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                MultipleHttpCallManager.this.callCallbacks();
            }

        }.execute();
    }

    /**
     *
     */
    public void execute(){
        workCounter = new AtomicInteger(this.callsManager.size());
        switch (executionModel){
            case PARALLEL:
                for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()) {
                    entry.getValue().onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {
                            workCounter.decrementAndGet();
                            synchronized (workCounter){
                                workCounter.notify();
                            }

                        }
                    }).perform();
                }
                synchronized (workCounter){
                    try {
                        while (workCounter.get() > 0){
                            workCounter.wait();
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Intereptuted while performing multiple tasks..." + e.getMessage());
                        e.printStackTrace();
                    }
                }
                break;
            case SINGLE:
                for (Map.Entry<String, HttpCallManager> entry: callsManager.entrySet()){
                    try {
                        if (!entry.getValue().getCall().isExecuted())
                            entry.getValue().execute();
                    } catch (IOException e) {
                        // TODO notify
                        return;
                    }
                    workCounter.incrementAndGet();
                }
                break;
        }

    }

    private void callCallbacks(){
        if (MultipleHttpCallManager.this.callback != null) {
            MultipleHttpCallManager.this.callback.onPostExecute();
            if (MultipleHttpCallManager.this.isAllSuccess()) {
                MultipleHttpCallManager.this.callback.onAllSuccess();
            } else {
                MultipleHttpCallManager.this.callback.onSomeFailure();
            }
        }
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
        if (manager == null) return false;
        return !manager.hasError() && manager.hasResponse() && manager.getResponse().isSuccessful();
    }

    @Override
    public void cancel() {
        if (workCounter == null) return;
        synchronized (callsManager){
            for (Map.Entry<String, HttpCallManager> call: callsManager.entrySet()){
                call.getValue().cancel();
            }
            this.callsManager.clear();
        }
    }

    @Override
    public boolean isDone() {
        return workCounter == null || workCounter.get() == 0;
    }

    // --------

    public static class Callback{

        public void onPostExecute() {};

        public void onAllSuccess() {};

        public void onSomeFailure() {};
    }
}
