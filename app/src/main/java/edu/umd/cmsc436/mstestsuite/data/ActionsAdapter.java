package edu.umd.cmsc436.mstestsuite.data;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import edu.umd.cmsc436.mstestsuite.R;

/**
 * Display the main actions of the frontend
 */

public class ActionsAdapter extends RecyclerView.Adapter<ItemHolder> {

    private Action[] mActions;

    private static final int VIEWTYPE_HEADER = 0;
    private static final int VIEWTYPE_ITEM = 1;

    private String mHeader;

    public ActionsAdapter (Action[] actions, String headerText) {
        this.mActions = actions;
        this.mHeader = headerText;
    }

    public void setHeader (String s) {
        this.mHeader = s;
        notifyItemChanged(0);
    }

    public void setEnabled(int position, boolean isEnabled) {
        mActions[position].setEnabled(isEnabled);
        notifyItemChanged(position+1);
    }

    public void setActions (Action[] actions) {
        mActions = actions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEWTYPE_HEADER : VIEWTYPE_ITEM;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout_resource = viewType == VIEWTYPE_HEADER ?
                R.layout.recyclerview_header : R.layout.recyclerview_item;

        FrameLayout fl = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(layout_resource, parent, false);

        return new ItemHolder(fl);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        if (position == 0) {
            holder.label.setText(mHeader);
            return;
        }

        holder.icon.setImageResource(mActions[position - 1].getIconResource());
        holder.label.setText(mActions[position - 1].getDisplayName());

        if (mActions[position-1].isEnabled()) {
            holder.root.setAlpha(1.0f);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActions[holder.getAdapterPosition() - 1].run();
                }
            });
        } else {
            holder.root.setAlpha(.5f);
            holder.root.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return mActions.length+1;
    }

    public GridLayoutManager.SpanSizeLookup getSpanLookup (final RecyclerView.LayoutManager layoutManager) {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    try {
                        return ((GridLayoutManager) layoutManager).getSpanCount();
                    } catch (ClassCastException cce) {
                        // fall through to return 1
                    }
                }

                return 1;
            }
        };
    }
}
