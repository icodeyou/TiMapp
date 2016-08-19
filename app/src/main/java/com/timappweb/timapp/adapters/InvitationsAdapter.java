package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.databinding.ItemInvitationBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InvitationsAdapter extends MyFlexibleAdapter {
    private static final String TAG = "InvitationsAdapter";


    public InvitationsAdapter(Context context) {
        super(context);
    }

    public void add(EventsInvitation eventsInvitation) {
        addItem(getItemCount(), new InvitationItem(eventsInvitation));
    }

}