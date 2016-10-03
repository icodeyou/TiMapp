package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Stephane on 16/08/2016.
 */
public class UserItem extends AbstractFlexibleItem<UserItem.FriendViewHolder> {

    private static final String TAG = "SubUserItem";
    private User user;

    public UserItem(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserItem that = (UserItem) o;
        return user != null ? user.equals(that.user) : that.user == null;
    }

    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_user;
    }

    @Override
    public FriendViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(getLayoutRes(), parent, false);
        return new FriendViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, FriendViewHolder holder, int position, List payloads) {
        UserItem item = (UserItem) adapter.getItem(position);
        User friend = item.getUser();
        holder.personName.setText(friend.getUsername());

        /*// Horizontal Tags Adapter. WORKING
        //...............................................
        Context context = MyApplication.getApplicationBaseContext();
        //Listener Horizontal Scroll View
        // Make it scrollable but it's also possible to click. Other wise if user click on tags
        // It does not react as a click on the whole element.
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, new OnItemAdapterClickListener() {
                    @Override
                    public void onClick(int position) {
                        if (adapter.mItemClickListener != null)
                            adapter.mItemClickListener.onItemClick(position);
                    }
                }, position);
        holder.horizontalTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //Horizontal tags
        HorizontalTagsAdapter horizontalTagsAdapter = holder.horizontalTags.getAdapter();
        if(!friend.hasTags()) {
            List<Tag> newbieList = new ArrayList<>();
            newbieList.add(new Tag(context.getString(R.string.newbie_tag)));
            horizontalTagsAdapter.setData(newbieList);
        } else {
            horizontalTagsAdapter.setData(friend.getTags());
        }
*/
        //User pic
        Uri uri = Uri.parse(friend.getProfilePictureUrl());
        holder.personPhoto.setImageURI(uri);
    }

    public class FriendViewHolder extends FlexibleViewHolder {

        View cv;
        TextView personName;
        SimpleDraweeView personPhoto;
        HorizontalTagsRecyclerView horizontalTags;

        FriendViewHolder(View itemView, FlexibleAdapter adapter) {
            super(itemView, adapter);
            cv = itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.tv_username);
            personPhoto = (SimpleDraweeView) itemView.findViewById(R.id.profile_picture);
            horizontalTags = (HorizontalTagsRecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);

            //TODO STEPH : Add tags !
            horizontalTags.setVisibility(View.GONE);

            Util.setSelectionsBackgroundAdapter(itemView, R.color.white, R.color.colorAccentLight, R.color.LightGrey);
        }

    }
}
