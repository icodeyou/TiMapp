package com.timappweb.timapp.listeners;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Instrumentation;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;

import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;

public class OnBasicQueryTagListener implements SearchView.OnQueryTextListener {

    protected SearchAndSelectTagManager manager;

    public OnBasicQueryTagListener() {
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.contains(" ")) {
            if(newText.length()<2) {
                //if this action is removed, onQueryTextChange will not be called
                // the second time the spacebar is pressed.
                simulateKeys();
            }
            newText = newText.substring(0, newText.length()-1);
            addTag(newText);
        }
        else {
            manager.suggestTag(newText);
        }
        return false;
    }

    private void simulateKeys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            }
        }).start();
    }

    public void addTag(String query) {
        manager.addTag(query);
        manager.getSearchView().setIconified(true);
    }

    public void setSearchAndSelectTagManager(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }
}
