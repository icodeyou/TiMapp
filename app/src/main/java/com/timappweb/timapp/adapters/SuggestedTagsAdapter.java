package com.timappweb.timapp.adapters;
//yo
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SuggestedTagsAdapter extends RecyclerView.Adapter<SuggestedTagsAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Tag> data = Collections.emptyList();

    public SuggestedTagsAdapter(Context context, List<Tag> data) {
        inflater = LayoutInflater.from(context);
        if (data != null)
            this.data = data;
    }

    public void setData(List<Tag> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
    public List<Tag> getData() {
        return this.data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View saved_tags_view = inflater.inflate(R.layout.design_suggested_tag, parent, false);
        MyViewHolder holder = new MyViewHolder(saved_tags_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Tag current = data.get(position);
        holder.tv_suggested_tag.setText(current.name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_suggested_tag;

        public MyViewHolder(View saved_tags_view) {
            super(saved_tags_view);
            tv_suggested_tag = (TextView) saved_tags_view.findViewById(R.id.item_suggested_tag);
        }
    }
}
