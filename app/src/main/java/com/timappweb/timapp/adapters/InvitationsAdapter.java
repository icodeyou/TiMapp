package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.databinding.ItemInvitationBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.InvitationsViewHolder> {
    private static final String TAG = "InvitationsAdapter";
    private Context context;

    private List<EventsInvitation> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public InvitationsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public InvitationsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ItemInvitationBinding mBinding  = DataBindingUtil.inflate(inflater, R.layout.item_invitation, viewGroup, false);
        return new InvitationsViewHolder(mBinding.getRoot(), mBinding);
    }

    @Override
    public void onBindViewHolder(InvitationsViewHolder baseHolder, int position) {
        InvitationsViewHolder holder = (InvitationsViewHolder) baseHolder;
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        final EventsInvitation placeInvitation = data.get(position);
        holder.setEventInHolder(placeInvitation);
        Log.d(TAG, "Done.");
    }

    @Override
    public int getItemCount() {
        return data.size();
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

    public class InvitationsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        ItemInvitationBinding mBinding;
        private SimpleTimerView tvCountPoints;
        private TextView titleCategory;
        private TextView titleEvent;

        InvitationsViewHolder(View itemView, ItemInvitationBinding binding) {
            super(itemView);
            mBinding = binding;

            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.points_text);
            titleCategory = (TextView) itemView.findViewById(R.id.title_category);
            titleEvent = (TextView) itemView.findViewById(R.id.name_event);
            titleCategory.setVisibility(View.VISIBLE);
            titleEvent.setVisibility(View.GONE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }

        public void setEventInHolder(EventsInvitation eventInvitation) {
            mBinding.setInvitation(eventInvitation);
            mBinding.setEvent(eventInvitation.event);
            mBinding.setUser(eventInvitation.getUserSource());


            //TODO : Following code is duplicated (method setEventInHolder() in class EventsAdapter)
            int initialTime = eventInvitation.event.getPoints();
            tvCountPoints.initTimer(initialTime);

            try {
                titleCategory.setText(Util.capitalize(eventInvitation.event.getCategory().name));
            } catch (UnknownCategoryException e) {
                e.printStackTrace();
            }

            // Following line is important, it will force to load the variable in a custom view
            mBinding.executePendingBindings();
        }
    }
}