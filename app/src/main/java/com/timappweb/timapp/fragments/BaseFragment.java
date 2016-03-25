package com.timappweb.timapp.fragments;

import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by stephane on 3/25/2016.
 */
public class BaseFragment extends Fragment{


    private static final String TAG = "BaseFragment";
    protected List<Call> asynCalls = new LinkedList<>();

    @Override
    public void onPause() {
        Log.d(TAG, "ExploreTagsFragment::onPause() -> cancelling api calls");
        super.onPause();
        for(Call call: asynCalls){
            call.cancel();
        }
    }
}
