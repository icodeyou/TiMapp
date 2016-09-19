package com.timappweb.timapp.adapters.flexibleadataper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractModelItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableHeaderItem<T extends IFlexible>
		extends AbstractModelItem<ExpandableHeaderItem.ExpandableHeaderViewHolder>
		implements IExpandable<ExpandableHeaderItem.ExpandableHeaderViewHolder, T>,
		IHeader<ExpandableHeaderItem.ExpandableHeaderViewHolder>{

	long serialVersionUID = -1882711111814491060L;

	/* Flags for FlexibleAdapter */
	private boolean mExpanded = false;

	/* subItems list */
	private List<T> mSubItems;


	public ExpandableHeaderItem(String id, String title) {
		super(id);
		setTitle(title);
		//We start with header shown and expanded
		setHidden(false);
		setExpanded(true);
		//NOT selectable (otherwise ActionMode will be activated on long click)!
		setSelectable(false);
	}

	@Override
	public boolean isExpanded() {
		return mExpanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
	}

	@Override
	public int getExpansionLevel() {
		return 0;
	}

	@Override
	public List<T> getSubItems() {
		return mSubItems;
	}

	public int getSubItemsCount(){
		return mSubItems != null ? mSubItems.size() : 0;
	}

	public final boolean hasSubItems() {
		return mSubItems!= null && mSubItems.size() > 0;
	}

	public boolean removeSubItem(SubItem item) {
		return item != null && mSubItems.remove(item);
	}

	public boolean removeSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.remove(position);
			return true;
		}
		return false;
	}

	public void removeSubItems(){
		if (mSubItems != null)
			mSubItems.clear();
	}

	public void addSubItem(T subItem) {
		if (mSubItems == null)
			mSubItems = new ArrayList<T>();
		mSubItems.add(subItem);
	}

	public void addSubItem(int position, T subItem) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.add(position, subItem);
		} else
			addSubItem(subItem);
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_header_item;
	}

	@Override
	public ExpandableHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ExpandableHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ExpandableHeaderViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		holder.mSubtitle.setText(getSubtitle());
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static class ExpandableHeaderViewHolder extends ExpandableViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public ExpandableHeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
		}

		@Override
		protected boolean isViewExpandableOnClick() {
			return true;//true by default
		}

		@Override
		protected void expandView(int position) {
			super.expandView(position);
			//Let's notify the item has been expanded
			if (mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
		}

		@Override
		protected void collapseView(int position) {
			super.collapseView(position);
			//Let's notify the item has been collapsed
			if (!mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
		}

	}

	@Override
	public String toString() {
		return "ExpandableHeaderItem[" + super.toString() + "//SubItems" + mSubItems + "]";
	}

}