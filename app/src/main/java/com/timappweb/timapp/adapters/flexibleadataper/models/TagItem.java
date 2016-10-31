package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import me.grantland.widget.AutofitTextView;

/**
 * Created by Stephane on 16/08/2016.
 */
public class TagItem extends AbstractFlexibleItem<TagItem.TagViewHolder> {

    private static final String TAG = "TagItem";
    private Tag tag;

    public TagItem(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagItem that = (TagItem) o;
        return tag != null ? tag.equals(that.tag) : that.tag == null;
    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_tag_with_counter;
    }

    @Override
    public TagViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(getLayoutRes(), parent, false);
        return new TagViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, TagViewHolder holder, int position, List payloads) {
        TagItem item = (TagItem) adapter.getItem(position);
        Tag tag = item.getTag();
        holder.tagName.setText(tag.name);
        holder.tagCountRef.setText(String.valueOf(tag.count_ref));
    }

    public class TagViewHolder extends FlexibleViewHolder {

        TextView tagName;
        TextView tagCountRef;

        TagViewHolder(View itemView, FlexibleAdapter adapter) {
            super(itemView, adapter);
            tagName = (TextView) itemView.findViewById(R.id.tv_tag);
            tagCountRef = (AutofitTextView) itemView.findViewById(R.id.tv_tag_counter);
            Util.setSelectionsBackgroundAdapter(itemView, R.color.background, R.color.colorAccentLight, R.color.LightGrey);
        }

    }
}
