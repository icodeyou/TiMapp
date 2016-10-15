package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.LinkedList;
import java.util.List;

public class TagsAndCountersAdapter extends RecyclerView.Adapter<TagsAndCountersAdapter.ViewHolder>{

    private final Context context;
    private final LinkedList<EventTag> mData;

    private OnItemAdapterClickListener onLongClickListener;

    public TagsAndCountersAdapter(Context context) {
        this.mData = new LinkedList<>();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_with_counter, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setEventTag(position, mData.get(position));
    }

    public void addAll(List data) {
        this.mData.addAll(data);
    }

    public EventTag getTag(int position) {
        return this.mData.get(position-1);
    }

    public void clear() {
        this.mData.clear();
    }

    public void add(EventTag tag) {
        this.mData.add(tag);
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.onLongClickListener = itemAdapterClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

        private final TextView tagText;
        private final TextView tagCounter;
        private final LinearLayout mainLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tagText = (TextView) itemView.findViewById(R.id.tv_tag);
            tagCounter = (TextView) itemView.findViewById(R.id.tv_tag_counter);
            mainLayout = (LinearLayout) itemView.findViewById(R.id.main_layout);
            itemView.setOnLongClickListener(this);
        }

        void setEventTag(int position, EventTag tag) {
            tagText.setText(tag.getTagName());
            tagCounter.setText(String.valueOf(tag.count_ref));

            /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mainLayout.getLayoutParams();
            if (position % 2 == 1){
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            else {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            }
            mainLayout.setLayoutParams(params);*/
        }

        @Override
        public boolean onLongClick(View v) {
            if(onLongClickListener!=null) {
                onLongClickListener.onClick(getAdapterPosition());
            }
            return true;
        }
    }
}