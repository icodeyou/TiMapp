package com.timappweb.timapp.adapters;
//yo
import android.content.Context;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;

import java.util.List;

public class FilledTagsAdapter extends TagsAdapter {

    public FilledTagsAdapter(Context context, List<Tag> data) {
        super(context, data, R.id.item_filled_tags, R.layout.design_filled_tags);
    }

}
