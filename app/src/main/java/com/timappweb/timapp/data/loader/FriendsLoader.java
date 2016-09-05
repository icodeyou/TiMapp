package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.support.v4.content.Loader;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;

import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by Stephane on 18/08/2016.
 */
public class FriendsLoader extends DataLoader<UserFriend> {

    private static final String TAG = "FriendsLoader";
    private static final long MIN_AUTO_REFRESH_DELAY = 3600 * 24 * 1000;     // Automatic refresh after one day
    private static final long MIN_FORCE_REFRESH_DELAY = 30 * 1000;            // Must wait 30 sec before reload

    // -----------------------------------------------------------------------------------------

    public FriendsLoader(Context context, FriendsAdapter adapter, WaveSwipeRefreshLayout swipeAndRefreshLayout) {
        super(context);
        this.setHistoryItemInterface(MyApplication.getCurrentUser());
        this.setMinDelayForceRefresh(MIN_AUTO_REFRESH_DELAY);
        this.setMinDelayForceRefresh(MIN_FORCE_REFRESH_DELAY);
        this.setSwipeAndRefreshLayout(swipeAndRefreshLayout);
        this.setAdapter(adapter);
        this.setSyncOptions(new SyncAdapterOption()
                .setType(DataSyncAdapter.SYNC_TYPE_FRIENDS)
                .setHashId(historyItemInterface));
    }
    @Override
    protected Loader<List<UserFriend>> buildModelLoader() {
        return new AutoModelLoader(context, UserFriend.class, ((User)historyItemInterface).getFriendsQuery(), false);
    }


    public void onFinish(List<UserFriend> data){
        super.onFinish(data);
        adapter.removeItemsOfType(R.layout.item_friend);
        if (data != null){
            for (UserFriend friend: data){
                adapter.addItem(new UserItem(friend.userTarget));
            }
            adapter.notifyDataSetChanged();
        }

    }

}
