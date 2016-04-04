package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.PlacesInvitation;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.PlaceView;

import java.util.ArrayList;
import java.util.List;

public class InvitationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PlacesAdapter";
    private Context context;
    private int colorRes = -1;
    private boolean isTagsVisible;
    private boolean footerActive;

    private List<PlacesInvitation> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public InvitationsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_invitation, viewGroup, false);
        return new PlacesViewHolder(v);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder baseHolder, int position) {
        if(baseHolder instanceof PlacesViewHolder) {
            PlacesViewHolder holder = (PlacesViewHolder) baseHolder;
            Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
            final PlacesInvitation placeInvitation = data.get(position);
            
            holder.invitorName.setText(placeInvitation.getUserSource().getUsername());
            holder.placeView.setPlace(placeInvitation.place);

            //OnTagsRvClick : Same event as adapter click.
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
            holder.horizontalTagsRv.setOnTouchListener(mHorizontalTagsTouchListener);
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

    public void add(PlacesInvitation placesInvitation) {
        this.data.add(placesInvitation);
        notifyDataSetChanged();
    }

    public void setData(List<PlacesInvitation> placesInvitations) {
        this.data = placesInvitations;
        notifyDataSetChanged();
    }

    public List<PlacesInvitation> getData() {
        return data;
    }

    public PlacesInvitation getItem(int position) {
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

        PlaceView placeView;
        TextView invitorName;
        HorizontalTagsRecyclerView horizontalTagsRv;

        PlacesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            placeView = (PlaceView) itemView.findViewById(R.id.place_view);
            invitorName = (TextView) itemView.findViewById(R.id.name_invitor);
            horizontalTagsRv = placeView.getRvPlaceTags();
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }

    public class FooterPlacesViewHolder extends RecyclerView.ViewHolder {

        private final Button newEventButton;

        FooterPlacesViewHolder(View itemView) {
            super(itemView);
            newEventButton = (Button) itemView.findViewById(R.id.create_button);
            newEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPlace(context);
                }
            });

        }
    }
}