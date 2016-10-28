package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class SuggestedTagsAdapter extends RecyclerView.Adapter<SuggestedTagsAdapter.SuggestedTagsaViewHolder > {

    private static final String TAG = "SuggestedTagsAdapter";

    // =============================================================================================

    private Context context;
    private List<Tag> data;
    private View.OnClickListener itemAdapterClickListener;

    // =============================================================================================

    public SuggestedTagsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public SuggestedTagsaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_suggested, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemAdapterClickListener != null) {
                    itemAdapterClickListener.onClick(v);
                }
            }
        });
        return new SuggestedTagsaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SuggestedTagsaViewHolder viewHolder, final int position) {
        final Tag tag = data.get(position);
        //Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        viewHolder.tagTv.setText(tag.getName());
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Tag tag) {
        this.data.add(tag);
        notifyItemInserted(data.size());
    }

    public void remove(Tag tag) {
        int index = data.indexOf(tag);
        Log.d(TAG, "Index of clicked suggested tag : " + index);
        Log.d(TAG, "Size of clicked suggested tag : " + data.size());
        if(this.data.remove(tag)) notifyItemRemoved(index);
    }

    public void setData(List<Tag> tags) {
        this.data = tags;
        notifyDataSetChanged();
    }

    public List<Tag> getData() {
        return data;
    }

    public List<String> getTagStrings() {
        List<String> newList = new ArrayList<>();
        for (Tag tag : data) {
            newList.add(tag.getName());
        }
        return newList;
    }

    public Tag get(int position) {
        return this.data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(View.OnClickListener clickListener) {
        this.itemAdapterClickListener = clickListener;
    }

    public class SuggestedTagsaViewHolder extends RecyclerView.ViewHolder {
        TextView tagTv;

        SuggestedTagsaViewHolder(View itemView) {
            super(itemView);

            tagTv = (TextView) itemView.findViewById(R.id.item_siggested_tag);
        }
    }
}