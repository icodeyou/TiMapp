package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TagsAndCountersAdapter extends RecyclerView.Adapter<TagsAndCountersAdapter.ViewHolder>{

    private final Context context;
    private final LinkedList<Tag> mData;

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
        holder.setTag(position, mData.get(position));
    }

    public void addAll(List data) {
        this.mData.addAll(data);
    }

    public void clear() {
        this.mData.clear();
    }

    public void add(Tag tag) {
        this.mData.add(tag);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tagText;
        private final TextView tagCounter;

        public ViewHolder(View itemView) {
            super(itemView);
            tagText = (TextView) itemView.findViewById(R.id.tv_tag);
            tagCounter = (TextView) itemView.findViewById(R.id.tv_tag_counter);
        }

        public void setTag(int position, Tag tag) {
            tagText.setText(tag.name);
            tagCounter.setText(String.valueOf(tag.count_ref));
            /*if (position % 2 == 1){
                tagText.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
            else {
                tagText.setBackgroundColor(context.getResources().getColor(R.color.background_light));
            }*/
        }
    }
}