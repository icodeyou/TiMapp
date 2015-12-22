package com.timappweb.timapp.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPostActivity;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;

public class AddPostMainFragment extends Fragment {

    private View view;
    private Boolean         GroupMode;
    private LinearLayout    top_layout_if_not_loaded;
    private EditText    top_layout_if_no_group;
    private LinearLayout    top_layout_with_group;
    private LinearLayout    addTagsLayout;
    private LinearLayout    tabsLayout;
    private CheckBox        anonymousCheckbox;
    private AddPostActivity addPostActivity;
    private Menu            mainMenu;
    private Button          aloneButton;
    private Button          groupButton;
    private Button          postButton;
    private EditText        commentET;
    private TextView        commentValidateButton;
    private RecyclerView    selectedTagsRV;
    private LinearLayout    commentLayout;
    private TextView        commentTV;
    private TextView        addTagsTV;

    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post_main, container, false);
        this.view = view;

        setHasOptionsMenu(true);

        //Initialize variables
        addPostActivity = (AddPostActivity) getActivity();
        fragmentManager =   getFragmentManager();
        top_layout_if_not_loaded = (LinearLayout) view.findViewById(R.id.top_layout_if_not_loaded);
        top_layout_if_no_group = (EditText) view.findViewById(R.id.top_layout_without_group);
        top_layout_with_group = (LinearLayout) view.findViewById(R.id.top_layout_with_group);
        addTagsLayout = (LinearLayout) view.findViewById(R.id.add_tags_layout);
        tabsLayout = (LinearLayout) view.findViewById(R.id.alone_and_group_buttons);
        aloneButton = (Button) view.findViewById(R.id.alone_button);
        groupButton = (Button) view.findViewById(R.id.group_button);
        commentValidateButton = (TextView) view.findViewById(R.id.comment_validate);
        postButton = (Button) view.findViewById(R.id.button_submit_post);
        selectedTagsRV = (RecyclerView) view.findViewById(R.id.rv_main_selected_tags);
        commentLayout = (LinearLayout) view.findViewById(R.id.comment_edittext_layout);
        commentTV = (TextView) view.findViewById(R.id.comment_textview);
        anonymousCheckbox = (CheckBox) view.findViewById(R.id.anonymous_layout);
        addTagsTV = (TextView) view.findViewById(R.id.add_tags_tv);
        commentET = (EditText) view.findViewById(R.id.comment_edit_text);

        setVisibilites();
        hideSelectedTagsRV();

        //set listeners
        addTagsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPostActivity.displaySearchFragment();
            }
        });
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGroupClick();
            }
        });
        aloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAloneClick();
                //hide keyboard if displayed
                InputMethodManager imm = (InputMethodManager) addPostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(top_layout_if_no_group.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        selectedTagsRV.addOnItemTouchListener(new RecyclerItemTouchListener(addPostActivity, new RecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                addPostActivity.displaySearchFragment();
            }
        }));

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressDialog.show();
                addPostActivity.submitNewPost();
            }
        });
        commentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayEditComment();
            }
        });
        commentValidateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideEditComment();
                commentTV.setText(commentET.getText());
            }
        });


        return view;
    }

    private void setVisibilites() {
        onAloneClick();
        if (isTags()) {
            addTagsLayout.setVisibility(View.GONE);
            selectedTagsRV.setVisibility(View.VISIBLE);
        } else {
            addTagsLayout.setVisibility(View.VISIBLE);
            selectedTagsRV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_add_spot_main, menu);
        mainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(isCommentEditDisplayed()) {
                    hideEditComment();
                } else {
                    NavUtils.navigateUpFromSameTask(addPostActivity);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onGroupClick() {
        top_layout_if_not_loaded.setVisibility(View.GONE);
        top_layout_if_no_group.setVisibility(View.VISIBLE);
        top_layout_with_group.setVisibility(View.GONE);
        groupButton.setBackgroundColor(ContextCompat.getColor(addPostActivity, R.color.Navy));
        groupButton.setTextColor(ContextCompat.getColor(addPostActivity, R.color.White));
        aloneButton.setBackgroundColor(ContextCompat.getColor(addPostActivity, R.color.LightGrey));
        aloneButton.setTextColor(ContextCompat.getColor(addPostActivity, R.color.Black));
        GroupMode = true;
    }

    public void onAloneClick() {
        top_layout_if_not_loaded.setVisibility(View.GONE);
        top_layout_if_no_group.setVisibility(View.GONE);
        top_layout_with_group.setVisibility(View.GONE);
        aloneButton.setBackgroundColor(ContextCompat.getColor(addPostActivity, R.color.Navy));
        aloneButton.setTextColor(ContextCompat.getColor(addPostActivity, R.color.White));
        groupButton.setBackgroundColor(ContextCompat.getColor(addPostActivity, R.color.LightGrey));
        groupButton.setTextColor(ContextCompat.getColor(addPostActivity, R.color.Black));
        GroupMode = false;
    }

    public void displayEditComment() {
        commentLayout.requestFocus();

        commentET.setText(commentTV.getText());

        // Set Visibilities
        commentLayout.setVisibility(View.VISIBLE);
        tabsLayout.setVisibility(View.GONE);
        top_layout_if_not_loaded.setVisibility(View.GONE);
        top_layout_if_no_group.setVisibility(View.GONE);
        top_layout_with_group.setVisibility(View.GONE);
        addTagsTV.setVisibility(View.GONE);
        anonymousCheckbox.setVisibility(View.GONE);
        commentTV.setVisibility(View.GONE);
        postButton.setVisibility(View.GONE);
        selectedTagsRV.setVisibility(View.GONE);
        addTagsLayout.setVisibility(View.GONE);
    }

    public void hideEditComment() {
        commentLayout.setVisibility(View.GONE);

        tabsLayout.setVisibility(View.VISIBLE);
        selectedTagsRV.setVisibility(View.VISIBLE);
        addTagsTV.setVisibility(View.VISIBLE);
        anonymousCheckbox.setVisibility(View.VISIBLE);
        commentTV.setVisibility(View.VISIBLE);
        postButton.setVisibility(View.VISIBLE);
        if (isTags()) {
            addTagsLayout.setVisibility(View.GONE);
            selectedTagsRV.setVisibility(View.VISIBLE);
        } else {
            addTagsLayout.setVisibility(View.VISIBLE);
            selectedTagsRV.setVisibility(View.GONE);
        }

        if (GroupMode == true) {
            onGroupClick();
        } else {
            onAloneClick();
        }
    }

    public void hideAddTagsLayout() {
        addTagsLayout.setVisibility(View.GONE);
    }

    public void displayAddTagsLayout() {
        addTagsLayout.setVisibility(View.VISIBLE);
    }

    public void hideSelectedTagsRV() {
        getSelectedTagsRV().setVisibility(View.GONE);
    }

    public void displaySelectedTagsRV() {
        getSelectedTagsRV().setVisibility(View.VISIBLE);
    }

    public boolean isCommentEditDisplayed() {
        if (commentLayout.getVisibility()==View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    public void saveComment()  {

    }

    public boolean isTags() {
        return getSelectedTagsRV().getAdapter().getItemCount() != 0;
    }

    public RecyclerView getSelectedTagsRV() {
        return selectedTagsRV;
    }

    public TextView getCommentTV() {
        return commentTV;
    }
}
