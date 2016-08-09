package com.timappweb.timapp.adapters.flexibleadataper;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractFlexibleItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class UserItem extends AbstractModelItem<UserItem.UserViewHolder>
		implements ISectionable<UserItem.UserViewHolder, IHeader>, IFilterable {

	private static final long 		serialVersionUID 		= 2519281529221244211L;
	private static final String 	TAG 					= "UserItem";

	// ---------------------------------------------------------------------------------------------

	private User 					user;
	IHeader 						header;

	// ---------------------------------------------------------------------------------------------

	public UserItem(String id, User user) {
		super(id);
		this.user = user;
	}

	public UserItem(String id, User user, ExpandableHeaderItem header) {
		this(id, user);
		this.setHeader(header);
	}

	@Override
	public IHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(IHeader header) {
		this.header = header;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_userplace;
	}

	@Override
	public UserViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new UserViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, UserViewHolder holder, int position, List payloads) {
		//In case of searchText matches with Title or with an SimpleItem's field
		// this will be highlighted
		if (adapter.hasSearchText()) {
			Utils.highlightText(holder.itemView.getContext(), holder.tvUsername,
					user.getUsername(), adapter.getSearchText(), R.color.colorAccent);
		} else {
			holder.tvUsername.setText(user.getUsername());
		}

		final String pic = user.getProfilePictureUrl();
		if(pic !=null) {
			Uri uri = Uri.parse(pic);
			holder.ivProfilePicture.setImageURI(uri);
		}
		holder.tvTime.setText(user.getTimeCreated());
		holder.user = user;

		//This "if-else" is just an example of what you can do with item animation
		/*
		if (adapter.isSelected(position)) {
			adapter.animateView(holder.itemView, position, true);
		} else {
			adapter.animateView(holder.itemView, position, false);
		}*/
	}



	@Override
	public boolean filter(String constraint) {
		return user.getUsername() != null && user.getUsername().toLowerCase().trim().contains(constraint);
	}

	@Override
	public String toString() {
		return "SubItem[" + super.toString() + "]";
	}

	public class UserViewHolder extends FlexibleViewHolder{

		User 							user;
		View 							cv;
		TextView 						tvUsername;
		TextView 						tvTime;
		RecyclerView 					rvPostTags;
		SimpleDraweeView 				ivProfilePicture;

		// ---------------------------------------------------------------------------------------------

		UserViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			itemView.setOnClickListener(this);
			cv = itemView.findViewById(R.id.cv);
			tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
			tvTime = (TextView) itemView.findViewById(R.id.tv_time);
			rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
			ivProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.profile_picture);
		}

		@Override
		public float getActivationElevation() {
			return Utils.dpToPx(itemView.getContext(), 4f);
		}

		@Override
		public void onClick(View view) {
			super.onClick(view);
			Log.v(TAG, "Click on user item: " + user);
			IntentsUtils.profile(user);
		}
	}

}