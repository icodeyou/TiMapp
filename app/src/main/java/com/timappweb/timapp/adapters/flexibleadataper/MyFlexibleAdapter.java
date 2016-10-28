package com.timappweb.timapp.adapters.flexibleadataper;

import android.content.Context;

import com.timappweb.timapp.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.ISectionable;

/**
 *
 */
public class MyFlexibleAdapter extends FlexibleAdapter<AbstractFlexibleItem> {

	private static final String TAG = "MyFlexibleAdapter";
	public int removeAllOffset = 0;

	// ---------------------------------------------------------------------------------------------

	protected Context mContext;
	private ArrayList<AbstractFlexibleItem> mItemsCopy;
	private OnItemLongClickListener mLongItemClickListener;

	// ---------------------------------------------------------------------------------------------

	public MyFlexibleAdapter(Context context) {
		super(new LinkedList<AbstractFlexibleItem>(), context);
		mContext = context;
		mItemsCopy = new ArrayList<>();
		setNotifyChangeOfUnfilteredItems(true);
	}

	public synchronized boolean addItem(AbstractFlexibleItem item){
		return this.addItem(getItemCount(), item);
	}

	public synchronized boolean addBeginning(List<AbstractFlexibleItem> items){
		return this.addItems(removeAllOffset, items);
	}

	public void addBeginning(AbstractFlexibleItem item) {
		this.addItem(0, item);
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

	public synchronized void addSubItem(ExpandableHeaderItem headerItem, ISectionable item) {
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

	public boolean hasData() {
		return this.getDataCount() > 0;
	}

	public void removeProgressItem() {
		this.removeItemsOfType(R.layout.progress_item);
	}

	/**
	 * For filtering purpose. The data copy must be done first
	 * @return
     */
	public ArrayList<AbstractFlexibleItem> createItemsCopy() {
		mItemsCopy = new ArrayList<>(getItemCount());
		for (int i = 0; i < getItemCount(); i++){
			mItemsCopy.add(getItem(i));
		}
		return mItemsCopy;
	}

	public ArrayList<AbstractFlexibleItem> getItemsCopy() {
		return mItemsCopy;
	}

	public synchronized void removeAll() {
		this.removeRange(removeAllOffset, getItemCount());
	}

	public synchronized void onLoadMoreComplete(List<AbstractFlexibleItem> items) {
		super.onLoadMoreComplete(items);
	}

	public int getDataCount() {
		return this.getItemCount() - removeAllOffset;
	}
}