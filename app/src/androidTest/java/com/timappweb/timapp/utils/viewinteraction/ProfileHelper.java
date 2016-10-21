package com.timappweb.timapp.utils.viewinteraction;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;

/**
 * Created by Stephane on 21/10/2016.
 */

public class ProfileHelper {

    private final ListViewHelper<Tag> tagsLV;

    public ProfileHelper() {
        tagsLV = new ListViewHelper<Tag>(R.id.listview_usertags, Tag.class);
    }

    public void assertHasTag(String value) {
        tagsLV.checkWithText(value);
    }

    public void assertCountTags(int count) {
        tagsLV.checkItemCount(count);
    }
}
