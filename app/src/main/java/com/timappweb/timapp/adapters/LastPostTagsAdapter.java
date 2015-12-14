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
import java.util.List;

public class LastPostTagsAdapter extends RecyclerView.Adapter<LastPostTagsAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private List<Tag> data = Collections.emptyList();

    public LastPostTagsAdapter(Context context, List<Tag> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public List<Tag> getData() {
        return this.data;
    }
    public void setData(List<Tag> data){
        this.data = data;
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View saved_tags_view = inflater.inflate(R.layout.design_last_post_tag, parent, false);
        MyViewHolder holder = new MyViewHolder(saved_tags_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Tag current = data.get(position);
        holder.tv_selected_tag.setText(current.name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_selected_tag;

        public MyViewHolder(View saved_tags_view) {
            super(saved_tags_view);
            tv_selected_tag = (TextView) saved_tags_view.findViewById(R.id.item_selected_tag);
        }
    }
}
