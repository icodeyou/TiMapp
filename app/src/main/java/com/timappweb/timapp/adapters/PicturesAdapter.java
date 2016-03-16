package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;


public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PictureViewHolder> {
    private static final String TAG = "PlaceUsersAdapter";

    List<Picture> data = new ArrayList<>();
    OnItemAdapterClickListener mItemClickListener;
    Context context;

    //Constructor
    public PicturesAdapter(Context context) {
        this.context = context;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_picture, viewGroup, false);
        context = viewGroup.getContext();

        return new PictureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PictureViewHolder pictureViewHolder, int position) {
        Picture picture = data.get(position);

        Picasso.with(context).load(picture.url).into(pictureViewHolder.ivPicture);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setData(List<Picture> pictures) {
        this.data = pictures;
        notifyDataSetChanged();
    }

    public void addDummyData() {
        User user = MyApplication.getCurrentUser();
        this.data.add(new Picture(user));
        this.data.add(new Picture(user));
        this.data.add(new Picture(user));
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);
    }


    public class PictureViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        ImageView ivPicture;

        PictureViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ivPicture = (ImageView) itemView.findViewById(R.id.picture);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
