package com.timappweb.timapp.adapters.flexibleadataper;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.timappweb.timapp.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;

/**
 *
 */
public class MyFlexibleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = MyFlexibleAdapter.class.getSimpleName();
	private Context mContext;//this should not be necessary for view holders


	public MyFlexibleAdapter(Context context) {
		super(new LinkedList<AbstractFlexibleItem>(), context);
		mContext = context;
		setNotifyChangeOfUnfilteredItems(true);
	}



	public int removeItems(ExpandableHeaderItem headerItem) {
		int headerPosition = getGlobalPositionOf(headerItem);
		int size = headerItem.getSubItems() != null ? headerItem.getSubItems().size() : 0;
		if (size > 0) {
			if (headerItem.isExpanded()) {
				removeRange(headerPosition + 1, size);
			}
			headerItem.removeSubItems();
		}
		return size;
	}

	public void addSubItem(ExpandableHeaderItem headerItem, UserItem item) {
		int size = headerItem.getSubItems() != null ? headerItem.getSubItems().size(): 0;
		if (headerItem.isExpanded()){
			addItemToSection(item, headerItem, size);
		}
		headerItem.addSubItem(item);
		notifyItemChanged(getGlobalPositionOf(headerItem));
	}

	public void expand(ExpandableHeaderItem headerItem) {
		if (headerItem.isExpanded()) return;
		this.expand(getGlobalPositionOf(headerItem));
	}

	public AbstractFlexibleItem getLastItem() {
		return this.getItem(this.getItemCount() - 1);
	}
}