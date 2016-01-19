package com.timappweb.timapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;

public class AddPlaceActivity extends BaseActivity {
    private String TAG = "PublishActivity";
    private InputMethodManager imm;

    //Views
    private EditText locationET;
    RecyclerView categoriesRV;
    CategoriesAdapter categoriesAdapter;
    private Category categorySelected;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        this.initToolbar(true);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        locationET = (EditText) findViewById(R.id.location_edit_text);
        categoriesRV = (RecyclerView) findViewById(R.id.rv_categories);

        initAdapterAndManager();
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    private void initAdapterAndManager() {
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    public RecyclerView getCategoriesRV() {
        return categoriesRV;
    }

    // ----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(Category category) {
        categorySelected = category;
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
