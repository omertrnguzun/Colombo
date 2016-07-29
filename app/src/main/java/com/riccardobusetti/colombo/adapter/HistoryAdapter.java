package com.riccardobusetti.colombo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.data.HistoryData;
import com.riccardobusetti.colombo.holder.MyHolderHistory;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<MyHolderHistory>{

    Context c;
    ArrayList<HistoryData> historyDatas;
    private int lastPosition = -1;

    public HistoryAdapter(Context c, ArrayList<HistoryData> historyDatas) {
        this.c = c;
        this.historyDatas = historyDatas;
    }

    //Inzializzazione ViewHolder
    @Override
    public MyHolderHistory onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_layout, parent, false);
        MyHolderHistory holder = new MyHolderHistory(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolderHistory holder, int position) {
        holder.name.setText(historyDatas.get(position).getTitle());
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(new Random().nextInt(501));//to make duration random number between [0,501)
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return historyDatas.size();
    }

}
