package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.auth.AuthManager;
import com.timappweb.timapp.auth.SocialProvider;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.loader.paginate.PaginateFilterFactory;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.UserFriend_Table;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import retrofit2.Response;

/**
 * Created by Stephane on 18/08/2016.
 */
public class FriendsLoaderFactory {

    private static final long   MIN_DELAY_FORCE_REFRESH = 30 * 1000;
    private static final long   MIN_DELAY_SYNC_FRIENDS_WITH_PROVIDER = 30 * 1000;
    private static long lastFriendSync;

    private FriendsLoaderFactory() {}

    public static CursorPaginateDataLoader<UserFriend, UserFriend> create(){
        return CursorPaginateDataLoader.<UserFriend, UserFriend>create(
                    "UserFriends/index",
                    UserFriend.class
                )
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<UserFriend, UserFriend>() {
                    @Override
                    public UserFriend beforeSaveModel(UserFriend model) {
                        model.user_source = MyApplication.getCurrentUser();
                        return model;
                    }
                })
                .initCache("UserFriends:"+MyApplication.getCurrentUser().getRemoteId(), 0)
                .addFilter(PaginateFilterFactory.createSyncIdFilter(UserFriend_Table.id))
                .setLocalQuery(SQLite.select().from(UserFriend.class).where(UserFriend_Table.user_source_id.eq(MyApplication.getCurrentUser().id)))
                .setClearQuery(SQLite.delete().from(UserFriend.class).where(UserFriend_Table.user_source_id.eq(MyApplication.getCurrentUser().id)));
    }

    public static CursorPaginateManager<UserFriend> manager(final Context context, MyFlexibleAdapter mAdapter, final SwipeRefreshLayout mSwipeRefreshLayout) {
        final CursorPaginateManager<UserFriend> manager = new CursorPaginateManager<UserFriend>(context, mAdapter, FriendsLoaderFactory.create())
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<UserFriend>() {
                    @Override
                    public AbstractFlexibleItem createItem(UserFriend userFriend) {
                        return new UserItem(userFriend.user_target);
                    }
                })
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .enableEndlessScroll();
        mSwipeRefreshLayout.setOnRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!Util.isOlderThan(lastFriendSync, MIN_DELAY_SYNC_FRIENDS_WITH_PROVIDER)){
                    manager.onRefresh();
                }
                else{
                    try {
                        FriendsLoaderFactory.requestSyncFriends()
                            .onFinally(new HttpCallManager.FinallyCallback<Void>() {
                                @Override
                                public void onFinally(Response<Void> response, Throwable error) {
                                    if (error != null || !response.isSuccessful()){
                                        Toast.makeText(context, R.string.action_performed_not_successful, Toast.LENGTH_SHORT).show();
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                    else {
                                        manager.onRefresh();
                                        lastFriendSync = System.currentTimeMillis();
                                    }
                                }
                            });
                    } catch (AuthManager.NoProviderAccessTokenException e) {
                        // TODO [critical] refresh token and perform action again
                        Toast.makeText(context, R.string.action_performed_not_successful, Toast.LENGTH_SHORT).show();
                        if (BuildConfig.DEBUG){
                            e.printStackTrace();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
        return manager;
    }

    public static HttpCallManager<Void> requestSyncFriends() throws AuthManager.NoProviderAccessTokenException {
        String accessToken = MyApplication.getAuthManager().getProviderToken();
        JsonObject options = new JsonObject();
        options.addProperty("access_token", accessToken);
        return RestClient
                .buildCall(RestClient.service().requestSyncFriends(SocialProvider.FACEBOOK.toString(), options))
                .perform();
    }
}
