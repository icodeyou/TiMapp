package com.timappweb.timapp.adapters.flexibleadataper.models;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.ExpandableHeaderItem;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.SubItem;

/**
 * Created by Stephane on 19/09/2016.
 */
public class PeopleHeaderItem extends ExpandableHeaderItem {

    /**
     * If count is set, we use it to get the subtitle, otherwise we count the number of sub items
     */
    private int count = -1;

    private SubItem _moreDataItem = null;

    public PeopleHeaderItem(String id, String title) {
        super(id, title);
    }

    @Override
    public String getSubtitle() {
        int count = this.count != -1 ? this.count : this.getSubItemsCount();
        return MyApplication.getApplicationBaseContext().getResources().getQuantityString(R.plurals.number_people_event, count, count);
    }

    @Override
    public int getSubItemsCount() {
        return super.getSubItemsCount() - (this._moreDataItem != null ? 1 : 0);
    }

    /**
     * Update the item count. If we have less data than the actual count, we are
     * adding a new item to builder the total count
     * @param count
     */
    public void setCount(int count, MyFlexibleAdapter adapter) {
        // If there is more data than we are showing
        if (count > this.getSubItemsCount()){
            int countNotLoaded = count - this.getSubItemsCount();
            String title = MyApplication.getApplicationBaseContext().getResources().getQuantityString(R.plurals.number_people_event_remaining, countNotLoaded, countNotLoaded);
            if (this._moreDataItem == null){
                this._moreDataItem = new SubItem(this.getId() + "-more");
                this._moreDataItem.setTitle(title);
                adapter.addSubItem(this, this._moreDataItem);
            }
            else{
                this._moreDataItem.setTitle(title);
                adapter.notifyItemChanged(adapter.getGlobalPositionOf(this._moreDataItem));
            }
        }
        else if (this._moreDataItem != null){
            this.removeSubItem(this._moreDataItem);
            this._moreDataItem = null;
        }
        this.count = count;
    }

    @Override
    public void removeSubItems() {
        this._moreDataItem = null;
        super.removeSubItems();
    }
}
