package com.synthform.colombo.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.synthform.colombo.R;
import com.synthform.colombo.util.ItemClickListener;
import com.synthform.colombo.util.ItemLongClickListener;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView name;
    public RelativeLayout bookmarkContainer;
    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public MyHolder(View itemView) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.name);
        bookmarkContainer = (RelativeLayout) itemView.findViewById(R.id.bookmarkContainer);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    @Override
    public void onClick(View v) {

        this.itemClickListener.onItemClick(v, getLayoutPosition());

    }

    @Override
    public boolean onLongClick(View view) {

        this.itemLongClickListener.onItemLongClick(view, getLayoutPosition());

        return true;
    }

    public void setItemClickListener(ItemClickListener ic) {

        this.itemClickListener = ic;

    }

    public void setItemLongClickListener(ItemLongClickListener ilc) {

        this.itemLongClickListener = ilc;

    }
}
