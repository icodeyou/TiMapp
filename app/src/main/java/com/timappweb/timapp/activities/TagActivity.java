package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnThreeQueriesTagListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.PlaceView;

import java.util.LinkedList;

public class TagActivity extends BaseActivity{

    private String TAG = "TagActivity";

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private View progressBarView;
    private PlaceView placeView;
    private Place currentPlace = null;

    // @Bind(R.id.hashtags1)
    protected HashtagView suggestedTagsView;

    //others
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private View selectedTagsView;
    private Post currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that we gave the place as an extra parameter
        this.currentPlace = IntentsUtils.extractPlace(getIntent());
        this.currentPost = IntentsUtils.extractPost(getIntent());
        if (this.currentPlace == null || this.currentPost == null){
            Log.d(TAG, "Place is null");
            IntentsUtils.addPostStepLocate(this);
            return;
        }
        
        setContentView(R.layout.activity_tag);
        this.initToolbar(false);

        //Initialize variables
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        progressBarView = findViewById(R.id.progress_view);
        placeView = (PlaceView) findViewById(R.id.place_view);


        initPlaceView();
        initClickSelectedTag();

        setSelectedTagsViewGone();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchAndSelectTagManager != null){
            //Clear list of tags in case back button is pressed in PublishActivity
            searchAndSelectTagManager.resetSelectedTags();
            actionCounter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);
        searchView.requestFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        initListTags();

        //set hint for searchview
        final OnBasicQueryTagListener onBasicQueryTagListener = new OnThreeQueriesTagListener(this);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                suggestedTagsView,
                selectedTagsRV,
                onBasicQueryTagListener,
                new SearchTagDataProvider() {
                    @Override
                    public void onLoadEnds() {
                        getProgressBarView().setVisibility(View.GONE);
                    }
                }
        );


        //Initialize Query hint in searchview
        actionCounter();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                String query = searchView.getQuery().toString();
                if(query.isEmpty()) {
                    Toast.makeText(this, "ActionTypeName a tag before submitting", Toast.LENGTH_SHORT).show();
                }
                else {
                    searchView.setQuery(query, true);
                }
            case R.id.home:
                //NavUtils.navigateUpFromSameTask(this);//May be necessary To have a colored home button
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //----------------------------------------------------------------------------------------------
    //Private methods

    private void initListTags() {
        suggestedTagsView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;

                //TODO STEPH : Enlever l'item de la liste pour de vrai. Pas pour de faux comme ci dessous. Bah oui. Petit escroc.
                suggestedTagsView.removeItem(item);

                searchView.setQuery(tag.name, true);
                searchView.clearFocus();
            }
        });

        suggestedTagsView.setData(new LinkedList<Tag>(), new DataTransformTag());
    }

    private void initClickSelectedTag() {
        selectedTagsRV.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item");
                selectedTagsRV.getAdapter().removeData(position);
                actionCounter();
            }
        });
    }

    private void initPlaceView() {
        placeView.setPlace(currentPlace);
    }

    public void actionCounter() {
        switch (searchAndSelectTagManager.getSelectedTags().size()) {
            case 0:
                setSelectedTagsViewGone();
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_3));
                break;
            case 1:
                setSelectedTagsViewVisible();
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_2));
                searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                break;
            case 2:
                searchView.setQueryHint(getResources().getString(R.string.searchview_hint_1));
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 3:
                currentPost.setTags(searchAndSelectTagManager.getSelectedTags());
                searchView.clearFocus();
                IntentsUtils.addPostStepPublish(this, this.currentPlace, currentPost);
            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    private void setSelectedTagsViewGone() {
        findViewById(R.id.top_line_hrv).setVisibility(View.GONE);
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    private void setSelectedTagsViewVisible() {
        findViewById(R.id.top_line_hrv).setVisibility(View.VISIBLE);
        selectedTagsView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.VISIBLE);
    }

    public void simulateKeys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            }
        }).start();
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public View getProgressBarView() {
        return progressBarView;
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
