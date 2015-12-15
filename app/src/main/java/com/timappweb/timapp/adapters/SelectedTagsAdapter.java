package com.timappweb.timapp.adapters;
import android.content.Context;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;
import java.util.List;

public class SelectedTagsAdapter extends TagsAdapter {

    public SelectedTagsAdapter(Context context, List<Tag> data) {
        super(context, data, R.id.item_selected_tag, R.layout.design_selected_tag);
    }

}
