package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.databinding.ItemInvitationBinding;
import com.timappweb.timapp.listeners.FabListenerFactory;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Stephane on 16/08/2016.
 */
public class InvitationItem extends AbstractFlexibleItem<InvitationItem.InvitationsViewHolder> {

    private static final String TAG = "InvitationItem";
    private final Context context;
    private EventsInvitation invitation;

    public InvitationItem(Context context, EventsInvitation invitation) {
        this.invitation = invitation;
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvitationItem that = (InvitationItem) o;

        return invitation != null ? invitation.equals(that.invitation) : that.invitation == null;

    }

    @Override
    public int hashCode() {
        return invitation != null ? invitation.hashCode() : 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_invitation;
    }

    @Override
    public InvitationsViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        ItemInvitationBinding mBinding  = DataBindingUtil.inflate(inflater, getLayoutRes(), parent, false);
        return new InvitationsViewHolder(mBinding.getRoot(), mBinding, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, InvitationsViewHolder holder, int position, List payloads) {
        holder.setEventInHolder(this.invitation);
    }

    public EventsInvitation getInvitation() {
        return invitation;
    }

    public class InvitationsViewHolder extends FlexibleViewHolder{

        ItemInvitationBinding mBinding;
        private SimpleTimerView tvCountPoints;
        private TextView titleCategory;
        private TextView titleEvent;

        InvitationsViewHolder(View itemView, ItemInvitationBinding binding, FlexibleAdapter adapter) {
            super(itemView, adapter);
            mBinding = binding;

            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.points_text);
            titleCategory = (TextView) itemView.findViewById(R.id.title_category);
            titleEvent = (TextView) itemView.findViewById(R.id.name_event);

            titleCategory.setVisibility(View.VISIBLE);
            titleEvent.setVisibility(View.GONE);
        }

        public void setEventInHolder(final EventsInvitation eventInvitation) {
            mBinding.setInvitation(eventInvitation);
            mBinding.setEvent(eventInvitation.event);
            mBinding.setUser(eventInvitation.getUserSource());

            if (eventInvitation.event != null && eventInvitation.event.event_category != null) {
                titleCategory.setText(Util.capitalize(eventInvitation.event.event_category.name));
            }

            FabListenerFactory.setFabListener(context, itemView, eventInvitation.event);

            // Following line is important, it will force to load the variable in a custom view
            mBinding.executePendingBindings();
        }

    }
}
