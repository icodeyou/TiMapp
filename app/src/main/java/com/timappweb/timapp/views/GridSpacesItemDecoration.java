package com.timappweb.timapp.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * https://github.com/lawloretienne/ImageGallery/blob/master/library/src/main/java/com/etiennelawlor/imagegallery/library/view/GridSpacesItemDecoration.java
 */
public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {

    // region Member Variables
    private final int mSpace;
    private final int mNumOfColumns;
    // endregion

    // region Constructors
    public GridSpacesItemDecoration(int space, int numOfColumns) {
        mSpace = space;
        mNumOfColumns = numOfColumns;
    }
    // endregion

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        if(position < mNumOfColumns){
            outRect.top = mSpace;
        }

        if(mNumOfColumns == 3){
            if(position % mNumOfColumns == 0){
                outRect.left = mSpace;
                outRect.right = mSpace/2;
            } else if(position % mNumOfColumns == 1){
                outRect.left = mSpace/2;
                outRect.right = mSpace/2;
            } else {
                outRect.left = mSpace/2;
                outRect.right = mSpace;
            }
        } else if(mNumOfColumns == 4){
            if(position % mNumOfColumns == 0){
                outRect.left = mSpace;
                outRect.right = mSpace/2;
            } else if(position % mNumOfColumns == 1){
                outRect.left = mSpace/2;
                outRect.right = mSpace/2;
            } else if(position % mNumOfColumns == 2){
                outRect.left = mSpace/2;
                outRect.right = mSpace/2;
            } else {
                outRect.left = mSpace/2;
                outRect.right = mSpace;
            }
        }

        outRect.bottom = mSpace;
    }
}

