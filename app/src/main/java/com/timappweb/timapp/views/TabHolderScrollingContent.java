package com.timappweb.timapp.views;

/**
 * Created by stephane on 5/19/2016.
 */
public interface TabHolderScrollingContent {
    /**
     * Adjust content scroll position based on sticky tab bar position.
     */
    void adjustScroll(int tabBarTop);
}
