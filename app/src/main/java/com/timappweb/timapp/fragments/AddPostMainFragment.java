package com.timappweb.timapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FilledTagsAdapter;
import com.timappweb.timapp.views.FilledRecyclerView;

public class AddPostMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post_main, container, false);

        initAdapters(view);

        return view;
    }

    private void initAdapters(View view) {
        // Selected tags
        FilledRecyclerView rvSelectedTags = (FilledRecyclerView) view.findViewById(R.id.rv_main_selected_tags);
        FilledTagsAdapter filledTagsAdapter = (FilledTagsAdapter) rvSelectedTags.getAdapter();
        rvSelectedTags.setAdapter(filledTagsAdapter);
    }
}
