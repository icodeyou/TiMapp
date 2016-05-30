package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.databinding.ItemInvitationBinding;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "EventsAdapter";
    private Context context;
    private int colorRes = -1;
    private boolean isTagsVisible;
    private boolean footerActive;

    private List<EventsInvitation> data;

    private OnItemAdapterClickListener itemAdapterClickListener;
    private ItemInvitationBinding mBinding;

    public InvitationsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        mBinding  = DataBindingUtil.inflate(inflater, R.layout.item_invitation, viewGroup, false);
        return new PlacesViewHolder(mBinding.getRoot());

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder baseHolder, int position) {
        if(baseHolder instanceof PlacesViewHolder) {
            //PlacesViewHolder holder = (PlacesViewHolder) baseHolder;
            Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
            final EventsInvitation placeInvitation = data.get(position);
            mBinding.setInvitation(placeInvitation);
            mBinding.setEvent(placeInvitation.event);
            mBinding.setUser(placeInvitation.getUserSource());

            /*
            User userSource = placeInvitation.getUserSource();
            String username = userSource != null ? userSource.getUsername() : "Former user";
            String prettyTimeInvitation = placeInvitation.getTimeCreated();
            holder.nameInvitation.setText(username);
            holder.dateInvitation.setText(prettyTimeInvitation);*/

        }
    }

    @Override
    public int getItemCount() {
        if(footerActive) {
            return data.size()+1;
        } else {
            return data.size();
        }
    }

    public void add(EventsInvitation eventsInvitation) {
        this.data.add(eventsInvitation);
        notifyDataSetChanged();
    }

    public void setData(List<EventsInvitation> eventsInvitations) {
        this.data = eventsInvitations;
        notifyDataSetChanged();
    }

    public List<EventsInvitation> getData() {
        return data;
    }

    public EventsInvitation getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        //EventView eventView;
        TextView nameInvitation;
        TextView dateInvitation;
        //ImageView backgroundImage;

        PlacesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            //eventView = (EventView) itemView.findViewById(R.id.event_view);
            nameInvitation = (TextView) itemView.findViewById(R.id.name_invitation);
            dateInvitation = (TextView) itemView.findViewById(R.id.date_invitation);
            //backgroundImage = (ImageView) itemView.findViewById(R.remote_id.background_invitation);
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }
}