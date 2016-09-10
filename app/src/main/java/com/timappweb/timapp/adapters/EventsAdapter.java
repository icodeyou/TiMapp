package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.databinding.LayoutEventBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private static final String TAG = "EventsAdapter";

    // =============================================================================================

    private Context context;
    private List<Event> data;
    private OnItemAdapterClickListener itemAdapterClickListener;

    // =============================================================================================

    public EventsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       LayoutEventBinding mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_event, parent, false);
        return new EventsViewHolder(mBinding.getRoot(), mBinding);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder viewHolder, int position) {
        final Event event = data.get(position);
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        viewHolder.setEventInHolder(event);

        viewHolder.setListeners(event);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Event event) {
        this.data.add(event);
        notifyDataSetChanged();
    }

    public void setData(List<Event> events) {
        this.data = events;
        notifyDataSetChanged();
    }

    public List<Event> getData() {
        return data;
    }

    public Event getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View cameraButton;
        private final View tagButton;
        private final View inviteButton;
        private final View comingButton;
        private final ImageView imageView;
        SimpleTimerView tvCountPoints;
        TextView titleCategory;
        TextView titleEvent;
        LayoutEventBinding mBinding;

        EventsViewHolder(View itemView, LayoutEventBinding binding) {
            super(itemView);
            this.mBinding = binding;

            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.points_text);
            titleCategory = (TextView) itemView.findViewById(R.id.title_category);
            titleEvent = (TextView) itemView.findViewById(R.id.name_event);
            cameraButton = itemView.findViewById(R.id.action_camera);
            tagButton = itemView.findViewById(R.id.action_tag);
            inviteButton = itemView.findViewById(R.id.action_invite);
            comingButton = itemView.findViewById(R.id.action_coming);
            imageView = (ImageView)itemView.findViewById(R.id.background_image_event);

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

        public void setEventInHolder(Event event) {
            mBinding.setEvent(event);
            int initialTime = event.getPoints();
            tvCountPoints.initTimer(initialTime);


            // @warning: we cannot use binding for this because image cannot load.
            //if (event.hasPicture()){
            //    imageView.setImageURI(Uri.parse(Uri.decode(event.getPicture().getThumbnailUrl())));
            //}
            //else{
            //    imageView.setImageDrawable(event.getBackgroundImage(imageView.getContext()));
            //}

            // TODO use binding
            try {
                titleCategory.setText(Util.capitalize(event.getCategory().name));
            } catch (UnknownCategoryException e) {
                e.printStackTrace();
            }

            // Following line is important, it will force to load the variable in a custom view
            mBinding.executePendingBindings();
        }

        public void setListeners(final Event event) {
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_CAMERA);
                }
            });
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_TAGS);
                }
            });
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_PEOPLE);
                }
            });
            comingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.postEvent(context, event, IntentsUtils.ACTION_COMING);
                }
            });
        }
    }
}