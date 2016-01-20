package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.utils.IntentsUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HorizontalTagsAdapter extends RecyclerView.Adapter<HorizontalTagsAdapter.MyViewHolder> {
    private String TAG = "HorizontalTagsAdapter";

    protected LayoutInflater inflater;
    protected List<Tag> data = Collections.emptyList();
    private Context context;

    public HorizontalTagsAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View saved_tags_view = inflater.inflate(R.layout.item_horizontal_tags, parent, false);
        MyViewHolder holder = new MyViewHolder(saved_tags_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
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
        if (!this.data.contains(newTag) && newTag.isValid()){
            String hashtagString = "#" + newTag.getName();
            newTag.setName(hashtagString);
            this.data.add(newTag);
            this.notifyDataSetChanged();
        }
        else {
            String nameTag = newTag.getName();
            int lengthTag = nameTag.length();
            if(lengthTag == 1) {
                if(!nameTag.equals(" ")) {
                    Toast.makeText(context, R.string.toast_tiny_text_size, Toast.LENGTH_LONG).show();
                }
            }
            else if (lengthTag > 30) {
                Toast.makeText(context, R.string.toast_huge_text_size, Toast.LENGTH_LONG).show();
            }
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

    /*
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
    */

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.item_horizontal_tag);
        }
    }
}
