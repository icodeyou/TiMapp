package com.timappweb.timapp.fragments;


import com.timappweb.timapp.listeners.LoadingListener;

/**
 * Created by stephane on 4/6/2016.
 */
public abstract class  PlaceBaseFragment extends BaseFragment implements LoadingListener {

    public abstract void updateBtnVisibility();

    public abstract void loadData();

    public abstract void setProgressView(boolean visibility);

    public void onLoadStart(){
        setProgressView(true);
        updateBtnVisibility();
    }

    public void onLoadEnd(){
        setProgressView(false);
        updateBtnVisibility();
    }

}
