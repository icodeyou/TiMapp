package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
        friendsAdapter = new PlaceFriendsAdapter(this,generateData());
        lvFriendsHere.setAdapter(friendsAdapter);
    }

    public void onInviteClick(View view) {
        int lastPosition = friendsAdapter.addData(User.createDummy());
        lvFriendsHere.smoothScrollToPosition(lastPosition);

        //TODO : envoyer une notification à l'utilisateur
        //penser à sauver "l'état" (bundle) avant de revenir en arrière, et récupérer cet état
            //quand on revient sur l'activité, de façon à ce qu'on voit les posts affichés.
    }

    //Generate Data for ListView
    private ArrayList<User> generateData(){
        ArrayList<User> friends = new ArrayList<>();
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        friends.add(User.createDummy());
        return friends;
    }
}
