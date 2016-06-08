package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.dummy.DummyUserEventFactory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.TwoDimArray;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.w3c.dom.Text;

import java.util.LinkedList;

/**
 * @warning Sticky header does not work well along with the material view pager. Indeed the first header
 * is placed over the material view pager header.
 * The hack implemented in this class consist in adding a first empty element with an empty header to create
 * the required offset.
 *
 */
public class EventUsersHeaderAdapter extends EventUsersAdapter
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private static final String TAG = "EventUsersAdapter";

    private OnItemAdapterClickListener mItemClickListener;

    public EventUsersHeaderAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
        this.data = new TwoDimArray<>();

        // -----------------------------------------------------------------------------------------
        // hack because off the bug when using in a material view pager
        this.data.create(new SectionItem(UserPlaceStatusEnum.GONE, VIEW_TYPES.PLACEHOLDER_TOP));
        this.data.addOne(UserPlaceStatusEnum.GONE, DummyUserEventFactory.create());
        // -----------------------------------------------------------------------------------------

        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.HERE, VIEW_TYPES.HERE));
        this.data.addOne(UserPlaceStatusEnum.HERE, DummyUserEventFactory.create());
        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.COMING, VIEW_TYPES.COMING));
        this.data.addOne(UserPlaceStatusEnum.COMING, DummyUserEventFactory.create());
        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.INVITED, VIEW_TYPES.INVITED));
        this.data.addOne(UserPlaceStatusEnum.INVITED, DummyUserEventFactory.create());
    }

    @Override
    public long getHeaderId(int position) {
        return getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType){
            case VIEW_TYPES.PLACEHOLDER_TOP:
                View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.empty_layout, viewGroup, false);
                return new RecyclerView.ViewHolder(view) {};
            default:
                return super.onCreateViewHolder(viewGroup, viewType);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_place_people, parent, false);
        return new RecyclerView.ViewHolder(view) {};
    }


    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        TextView textView = (TextView) holder.itemView.findViewById(R.id.text_header_place_people);
        switch (type) {
            case VIEW_TYPES.PLACEHOLDER_TOP:
                //holder.itemView.setVisibility(View.GONE);
                ((TextView)holder.itemView).setHeight(0);
                //holder.itemView.setBackground(null);
                //textView.setText("TEST");
                break;
            case VIEW_TYPES.HERE:
                textView.setText(context.getResources().getString(R.string.header_posts));
                break;
            case VIEW_TYPES.COMING:
                textView.setText(context.getResources().getString(R.string.header_coming));
                break;
            case VIEW_TYPES.INVITED:
                textView.setText(context.getResources().getString(R.string.header_invited));
                break;
        }
    }


}
