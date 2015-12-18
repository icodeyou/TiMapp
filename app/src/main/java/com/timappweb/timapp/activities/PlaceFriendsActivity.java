package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlaceFriendsAdapter;
import com.timappweb.timapp.entities.User;

import java.util.ArrayList;

public class PlaceFriendsActivity extends BaseActivity {
    PlaceFriendsAdapter friendsAdapter;
    ListView lvFriendsHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_friends);

        this.initToolbar(true);

        //Listview - Friends
        lvFriendsHere = (ListView) findViewById(R.id.list_friends);
        friendsAdapter = new PlaceFriendsAdapter(this);
        friendsAdapter.initializeData();
        lvFriendsHere.setAdapter(friendsAdapter);
    }

    public void onInviteClick(View view) {
        User u = User.createDummy();
        u.setStatus(true);
        friendsAdapter.add(u);
        friendsAdapter.getCount();
        lvFriendsHere.smoothScrollToPosition(friendsAdapter.getCount());

        //TODO : envoyer une notification à l'utilisateur
    }

    //Generate Data for ListView
    private ArrayList<User> generateData(){
        ArrayList<User> friends = new ArrayList<>();
        return friends;
    }
}
