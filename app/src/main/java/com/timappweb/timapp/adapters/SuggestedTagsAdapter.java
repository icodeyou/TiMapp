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

public class SuggestedTagsAdapter extends TagsAdapter {

    public SuggestedTagsAdapter(Context context, List<Tag> data) {
        super(context, data, R.id.item_suggested_tag, R.layout.design_suggested_tag);
    }

}
