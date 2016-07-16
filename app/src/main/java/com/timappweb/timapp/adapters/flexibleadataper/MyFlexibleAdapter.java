package com.timappweb.timapp.adapters.flexibleadataper;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.timappweb.timapp.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

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


	public MyFlexibleAdapter(List items, Activity activity) {
		super(items, activity);
		mContext = activity;

		//NEW! We have highlighted text while filtering, so let's enable this feature
		//to be consistent with the active filter
		setNotifyChangeOfUnfilteredItems(true);
	}

	@Override
	public void updateDataSet(List<AbstractFlexibleItem> items) {
		super.updateDataSet(items);
		//Overwrite the list and fully notify the change
		//Watch out! The original list must a copy
		//TODO: We may create calls like removeAll, addAll or refreshList in order to animate changes
		//Add example view
		//addUserLearnedSelection(true);
	}

	/*
	 * HEADER/FOOTER VIEW
	 * This method show how to add Header/Footer View as it was for ListView.
	 * The secret is the position! 0 for Header; itemCount for Footer ;-)
	 * The view is represented by a custom Item type to better represent any dynamic content.
	 */
	/*
	public void addUserLearnedSelection(boolean scrollToPosition) {
		if (!DatabaseService.userLearnedSelection && !hasSearchText() && !(getItem(0) instanceof ULSItem)) {
			//Define Example View
			final ULSItem item = new ULSItem("ULS");
			item.setTitle(mContext.getString(R.string.uls_title));
			item.setSubtitle(mContext.getString(R.string.uls_subtitle));
			addItemWithDelay(0, item, 1700L, scrollToPosition);
		}
	}
	*/

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



}