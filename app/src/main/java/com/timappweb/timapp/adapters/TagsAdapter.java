package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.MyViewHolder> {
    private final int idTextView;
    protected LayoutInflater inflater;
    protected List<Tag> data = Collections.emptyList();
    private int idLayout;

    public TagsAdapter(Context context, List<Tag> data, int idTextView, int idLayout) {
        this.idTextView = idTextView;
        this.idLayout = idLayout;
        inflater = LayoutInflater.from(context);
        if (data != null)
            this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View saved_tags_view = inflater.inflate(idLayout, parent, false);
        MyViewHolder holder = new MyViewHolder(saved_tags_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Tag current = data.get(position);
        holder.textView.setText(current.name);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void setData(List<Tag> data) {
        this.data = data;
        this.notifyDataSetChanged();
    };

    public Tag getData(int position) {
        return this.data.get(position);
    }

    public List<Tag> getData() {
        return this.data;
    }
    public void addData(String selectedTag) {
        Tag newTag = new Tag(selectedTag, 0);
        if (!this.data.contains(newTag)){
            this.data.add(newTag);
            this.notifyDataSetChanged();
        }
    }

    public void removeData(int position) {
        this.data.remove(position);
        this.notifyDataSetChanged();
    }

    public void resetData() {
        this.data.clear();
        this.notifyDataSetChanged();
    }

    public ArrayList<String> getStringsFromTags() {
        ArrayList<String> res = new ArrayList<String>();
        for (Tag tag : this.data) {
            res.add(tag.getName());
        }
        return res;
    }

    public ArrayList<Tag> getTagsFromStrings(ArrayList<String> tagsString) {
        ArrayList<Tag> tagsList = new ArrayList<Tag>();
        for(String s : tagsString ) {
            Tag tag = new Tag(s);
            tagsList.add(tag);
        }
        return tagsList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(idTextView);
        }
    }
}
