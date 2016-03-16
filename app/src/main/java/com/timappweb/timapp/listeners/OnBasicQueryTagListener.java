package com.timappweb.timapp.listeners;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;

import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;

public class OnBasicQueryTagListener implements SearchView.OnQueryTextListener {

    private SearchAndSelectTagManager manager;

    public OnBasicQueryTagListener() {
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        manager.addTag(query);
        manager.getSearchView().setIconified(true);
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
            onQueryTextSubmit(newText);
        }
        else {
            if(manager!=null) {
                manager.suggestTag(newText);
            }
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

    public void setSearchAndSelectTagManager(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }
}
