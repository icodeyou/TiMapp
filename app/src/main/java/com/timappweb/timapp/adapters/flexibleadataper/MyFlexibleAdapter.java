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
 * NOTE: AbstractModelItem is for example purpose only. I wanted to have in common
 * some Fields and Layout.
 * You, having different Layout for each item type, would use IFlexible or AbstractFlexibleItem
 * as base item to extend this Adapter.
 */
public class MyFlexibleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = MyFlexibleAdapter.class.getSimpleName();

	public static final int CHILD_VIEW_TYPE = 0;
	public static final int EXAMPLE_VIEW_TYPE = 1;

	private Context mContext;//this should not be necessary for view holders


	public MyFlexibleAdapter(FragmentActivity activity) {
		super(new LinkedList<AbstractFlexibleItem>(), activity);
		mContext = activity;
		setNotifyChangeOfUnfilteredItems(true);
	}

	@Override
	public synchronized void filterItems(@NonNull List<AbstractFlexibleItem> unfilteredItems) {
		super.filterItems(unfilteredItems);
		//addUserLearnedSelection(false);
	}

	@Override
	public void selectAll(Integer... viewTypes) {
		super.selectAll();
	}


	@Override
	public List<Animator> getAnimators(View itemView, int position, boolean isSelected) {
		List<Animator> animators = new ArrayList<Animator>();
		if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
			//GridLayout
			if (position % 2 == 0)
				addSlideInFromRightAnimator(animators, itemView, 0.5f);
			else
				addSlideInFromLeftAnimator(animators, itemView, 0.5f);
		} else {
			//LinearLayout
			switch (getItemViewType(position)) {
				case R.layout.recycler_child_item:
				case EXAMPLE_VIEW_TYPE:
					addScaleInAnimator(animators, itemView, 0.0f);
					break;
				default:
					if (isSelected)
						addSlideInFromRightAnimator(animators, itemView, 0.5f);
					else
						addSlideInFromLeftAnimator(animators, itemView, 0.5f);
					break;
			}
		}

		//Alpha Animator is automatically added
		return animators;
	}

	@Override
	public String onCreateBubbleText(int position) {
		//if (!DatabaseService.userLearnedSelection && position == 0) {//This 'if' is for my example only
			//TODO FOR YOU: This is the normal line you should use: Usually it's the first letter
		//	return Integer.toString(position);
		//}
		return super.onCreateBubbleText(position);
	}



	/**
	 +	 * Provides all the item positions that belongs to the section represented by the specified header.
	 +	 *
	 +	 * @param header the header that represents the section
	 +	 * @return NonNull list of all item positions in the specified section.
	 */
	public int getEndHeaderPosition(@NonNull IHeader header) {
		int startPosition = getGlobalPositionOf(header);
		while (getItem(++startPosition) != null && !hasSameHeader(getItem(startPosition), header)) {
		}
		return startPosition;
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
}