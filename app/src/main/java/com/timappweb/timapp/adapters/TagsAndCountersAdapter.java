package com.timappweb.timapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.utils.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TagsAndCountersAdapter extends RecyclerView.Adapter<TagsAndCountersAdapter.ViewHolder> {

    private final Context context;
    private List<Tag> data;

    public TagsAndCountersAdapter(Context context) {
        super();
        this.context = context;
        this.data = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag_with_counter, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fillFields(data.get(position));
    }

    @Override
    public int getItemCount() {
        return this.data == null ? 0 : this.data.size();
    }

    public void clear() {
        this.data.clear();
    }

    public void addAll(List data) {
        this.data.addAll(data);
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tagText;
        private final TextView tagCounter;

        public ViewHolder(View itemView) {
            super(itemView);
            tagText = (TextView) itemView.findViewById(R.id.tv_tag);
            tagCounter = (TextView) itemView.findViewById(R.id.tv_tag_counter);
        }

        public void fillFields(Tag tag) {
            tagText.setText(tag.name);
            tagCounter.setText(tag.count_ref);
        }
    }
}