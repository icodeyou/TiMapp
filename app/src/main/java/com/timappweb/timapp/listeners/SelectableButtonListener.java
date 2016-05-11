package com.timappweb.timapp.listeners;

/**
 * Created by Jack on 10/05/2016.
 */
public interface SelectableButtonListener {


    boolean performEnabled();

    boolean performDisabled();

    void updateUI(boolean enabled);
}
