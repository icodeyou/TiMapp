package com.timappweb.timapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.entities.Tag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class HorizontalTagsAdapter extends RecyclerView.Adapter<HorizontalTagsAdapter.MyViewHolder> {
    private String TAG = "HorizontalTagsAdapter";

    protected LayoutInflater inflater;
    private List<Tag> data = new ArrayList<>();
    private Context context;

    private int textColor;
    private int backgroundColor;
    private boolean isBold;
    private Float textSize;

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    private OnItemAdapterClickListener itemAdapterClickListener;

    public HorizontalTagsAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_horizontal_tags, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Tag current = data.get(position);
        holder.textView.setText("#" + current.name);

        if (itemAdapterClickListener != null){
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void setDummyData() {
        data.add(new Tag("plongée"));
        data.add(new Tag("chillmusic"));
        data.add(new Tag("festival"));
        notifyDataSetChanged();
    }

    public void add(Tag tag) {
        data.add(tag);
        notifyDataSetChanged();
    }

    public void setData(List<Tag> data) {
        if(data!=null) {
            this.data = new ArrayList<>(data);
            notifyDataSetChanged();
        }
    };

    public Tag getData(int position) {
        return this.data.get(position);
    }

    public List<Tag> getData() {
        return this.data;
    }

    public boolean tryAddData(String selectedTag) {
        Tag newTag = new Tag(selectedTag, 0);
        if (this.data.contains(newTag)) {
            Toast.makeText(context, R.string.toast_tag_already_chosen, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(newTag.getName().isEmpty()) {
            Toast.makeText(context, R.string.toast_no_tag, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!newTag.isShortEnough()){
            Toast.makeText(context, R.string.toast_tiny_text_size, Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!newTag.isLongEnough()) {
            Toast.makeText(context, R.string.toast_huge_text_size, Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            newTag.setName(newTag.getName());
            data.add(newTag);
            notifyDataSetChanged();
            return true;
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

    public boolean isOneSimilarValue(String string) {
        for (Tag t : data) {
            if(t.getName().equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    public void setColors(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public void settextStyle(boolean isBold) {
        this.isBold = isBold;
    }

    public void settextSize(Float textSize) {
        this.textSize = textSize;
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
            view.setBackgroundColor(backgroundColor);
            textView.setTextColor(textColor);
            textView.setTextSize(Util.convertPixelsToDp(textSize,context));
            if(!isBold) {
                textView.setTypeface(Typeface.DEFAULT);
            } else {
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    }
}
