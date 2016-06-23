package com.riccardobusetti.colombo.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.util.ItemClickListener;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private TextView name,code;
    private ItemClickListener itemClickListener;

    public MyHolder(View itemView) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.name);
        code = (TextView) itemView.findViewById(R.id.code);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        this.itemClickListener.onItemClick(v,getLayoutPosition());

    }

    public void setItemClickListener(ItemClickListener ic) {

        this.itemClickListener = ic;

    }
}
