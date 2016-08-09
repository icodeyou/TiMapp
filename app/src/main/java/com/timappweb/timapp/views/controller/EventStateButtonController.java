package com.timappweb.timapp.views.controller;

import android.content.Context;
import android.view.View;

import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventStatus;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.utils.location.LocationManager;

/**
 * Created by stephane on 5/27/2016.
 */
public class EventStateButtonController extends ActivableButtonController {

    private final Event mEvent;
    private final Context mContext;
    private final UserPlaceStatusEnum mStatus;

    public EventStateButtonController(Context context, View view, Event event, UserPlaceStatusEnum status) {
        super(view);
        mEvent = event;
        mContext = context;
        mStatus = status;
    }

    @Override
    public boolean performActivate() {
        EventStatusManager.instance().add(mContext, mEvent, mStatus)
            .onResponse(new HttpCallback() {
                @Override
                public void successful(Object feedback) {
                    commitChange();
                }

                @Override
                public void notSuccessful() {
                    rollbackChange();
                }

            });
        return true;
    }

    @Override
    public boolean cancelActivate() {
        EventStatusManager.instance()
                .add(mContext, mEvent, mStatus)
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        commitChange();
                    }

                    @Override
                    public void notSuccessful() {
                        rollbackChange();
                    }

                })
                .onError(new RequestFailureCallback(){
                    @Override
                    public void onError(Throwable error) {
                        rollbackChange();
                    }
                });
        return true;
    }

    @Override
    public void initState() {
        if (!mEvent.isOver()){
            EventStatus eventStatus = EventStatusManager.getStatus(mEvent);
            // Has status
            if (eventStatus != null){
                if (eventStatus.status == mStatus){
                    mView.setVisibility(View.VISIBLE);
                    setActivated(true);
                }
                else{
                    mView.setVisibility(View.GONE);
                }
            }
            else{
                if (LocationManager.hasFineLocation()){
                    if (mEvent.isUserAround()){
                        mView.setVisibility(mStatus == UserPlaceStatusEnum.HERE ? View.VISIBLE : View.GONE);
                    }
                    else{
                        mView.setVisibility(mStatus == UserPlaceStatusEnum.COMING ? View.VISIBLE : View.GONE);
                    }
                }
                else{
                    mView.setVisibility(View.GONE);
                }
            }
        }
        else {
            mView.setVisibility(View.GONE);
        }
    }

}
