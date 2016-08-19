package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.databinding.ItemInvitationBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by Stephane on 16/08/2016.
 */
public class InvitationItem extends AbstractFlexibleItem<InvitationItem.InvitationsViewHolder> {

    private static final String TAG = "PictureItem";
    private EventsInvitation invitation;

    public InvitationItem(EventsInvitation invitation) {
        this.invitation = invitation;
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
        return new InvitationsViewHolder(mBinding.getRoot(), mBinding);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, InvitationsViewHolder holder, int position, List payloads) {
        Log.v(TAG, "Loading picture in adapter: " + invitation);
        holder.setEventInHolder(this.invitation);
    }

    public EventsInvitation getInvitation() {
        return invitation;
    }

    public class InvitationsViewHolder extends RecyclerView.ViewHolder{

        ItemInvitationBinding mBinding;
        private SimpleTimerView tvCountPoints;
        private TextView titleCategory;
        private TextView titleEvent;
        private final View cameraButton;
        private final View tagButton;
        private final View inviteButton;

        InvitationsViewHolder(View itemView, ItemInvitationBinding binding) {
            super(itemView);
            mBinding = binding;

            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.points_text);
            titleCategory = (TextView) itemView.findViewById(R.id.title_category);
            titleEvent = (TextView) itemView.findViewById(R.id.name_event);
            cameraButton = itemView.findViewById(R.id.action_camera);
            tagButton = itemView.findViewById(R.id.action_tag);
            inviteButton = itemView.findViewById(R.id.action_invite);

            titleCategory.setVisibility(View.VISIBLE);
            titleEvent.setVisibility(View.GONE);
        }

        public void setEventInHolder(final EventsInvitation eventInvitation) {
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

            // TODO do not set here
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(MyApplication.getApplicationBaseContext(), eventInvitation.event, IntentsUtils.ACTION_CAMERA);
                }
            });
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(MyApplication.getApplicationBaseContext(), eventInvitation.event, IntentsUtils.ACTION_TAGS);
                }
            });
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(MyApplication.getApplicationBaseContext(), eventInvitation.event, IntentsUtils.ACTION_PEOPLE);
                }
            });


            // Following line is important, it will force to load the variable in a custom view
            mBinding.executePendingBindings();
        }

    }
}
