package com.timappweb.timapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FilledTagsAdapter;
import com.timappweb.timapp.views.FilledRecyclerView;
import com.timappweb.timapp.views.HorizontalRecyclerView;

public class AddPostSearchFragment extends Fragment {

    private HorizontalRecyclerView selectedTagsRV;
    private FilledRecyclerView suggestedTagsRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post_search, container, false);

        initAdapters(view);

        return view;
    }

    public HorizontalRecyclerView getSelectedTagsRV() {
        return selectedTagsRV;
    }

    public FilledRecyclerView getSuggestedTagsRV() {
        return suggestedTagsRV;
    }
    private void initAdapters(View view) {
        // Selected tags
        selectedTagsRV = (HorizontalRecyclerView) view.findViewById(R.id.rv_search_selected_tags);
        suggestedTagsRV = (FilledRecyclerView) view.findViewById(R.id.rv_search_suggested_tags);
    }
}
