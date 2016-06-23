package com.riccardobusetti.colombo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.data.CardData;
import com.riccardobusetti.colombo.holder.MyHolder;
import com.riccardobusetti.colombo.util.ItemClickListener;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class MyAdapter extends RecyclerView.Adapter<MyHolder>{

    Context c;
    ArrayList<CardData> cardData;
    private int lastPosition = -1;

    public MyAdapter(Context c, ArrayList<CardData> cardData) {
        this.c = c;
        this.cardData = cardData;
    }

    //Inzializzazione ViewHolder
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Creazione View Object
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_layout, parent, false);

        //Creazione Holder
        MyHolder holder = new MyHolder(v);

        return holder;
    }

    //Inizialiazzione Bind
    @Override
    public void onBindViewHolder(final MyHolder holder, int position) {

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {

            }
        });

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
        return cardData.size();
    }

}
