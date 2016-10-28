package com.timappweb.timapp.data.loader;

import android.content.Context;

import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.models.UserFriend;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by Stephane on 18/08/2016.
 */
public class FriendsLoaderFactory {

    private static final long   MIN_DELAY_FORCE_REFRESH = 30 * 1000;

    private FriendsLoaderFactory() {}

    public static CursorPaginateDataLoader<UserFriend, UserFriend> create(){
        return CursorPaginateDataLoader.<UserFriend, UserFriend>create(
                    "UserFriends/index",
                    UserFriend.class
                )
                .initCache("UserFriends:"+MyApplication.getCurrentUser().getRemoteId(), 0)
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createCreatedFilter())
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createSyncIdFilter())
                .setLocalQuery(new Select().from(UserFriend.class).where("UserSource = ?", MyApplication.getCurrentUser().getId()));
    }

    public static CursorPaginateManager<UserFriend> manager(Context context, MyFlexibleAdapter mAdapter) {
        return new CursorPaginateManager<UserFriend>(context, mAdapter, FriendsLoaderFactory.create())
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<UserFriend>() {
                    @Override
                    public AbstractFlexibleItem createItem(UserFriend userFriend) {
                        return new UserItem(userFriend.userTarget);
                    }
                })
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .enableEndlessScroll();
    }
}
