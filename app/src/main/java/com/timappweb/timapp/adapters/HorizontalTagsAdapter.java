package com.timappweb.timapp.adapters;
import android.content.Context;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;
import java.util.List;

public class HorizontalTagsAdapter extends TagsAdapter {

    public HorizontalTagsAdapter(Context context, List<Tag> data) {
        super(context, data, R.id.item_horizontal_tags, R.layout.design_horizontal_tags);
    }

}
