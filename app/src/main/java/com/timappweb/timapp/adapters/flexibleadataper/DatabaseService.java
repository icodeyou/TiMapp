package com.timappweb.timapp.adapters.flexibleadataper;

import android.content.Context;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Project FlexibleAdapter.
 */
public class DatabaseService {

	private static DatabaseService mInstance;
	private static final int ITEMS = 90, SUB_ITEMS = 6, HEADERS = 30;

	//Database original items
	private List<AbstractFlexibleItem> mItems = new ArrayList<>();

	DatabaseService() {
	}

	public static DatabaseService getInstance() {
		if (mInstance == null) {
			mInstance = new DatabaseService();
		}
		return mInstance;
	}

	/*-------------------*/
	/* DATABASE CREATION */
	/*-------------------*/
	/*
	 * List of expandable items (headers/sections) with SubItems with Header attached.
	 */
	public void createExpandableSectionsDatabase(Context context) {
	}

	private AbstractFlexibleItem newPlaceHolderSection(int i) {
		return new PlaceHolderItem("PLACEHOLDER"+ i);
	}


	/*-----------------------*/
	/* MAIN DATABASE METHODS */
	/*-----------------------*/

	/**
	 * @return Always a copy of the original list.
	 */
	public List<AbstractFlexibleItem> getDatabaseList() {
		//Return a copy of the DB: we will perform some tricky code on this list.
		return new ArrayList<AbstractFlexibleItem>(mItems);
	}

}