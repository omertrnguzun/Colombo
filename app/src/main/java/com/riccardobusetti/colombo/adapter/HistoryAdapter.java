package com.riccardobusetti.colombo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.data.HistoryData;
import com.riccardobusetti.colombo.holder.MyHolderHistory;

import java.util.ArrayList;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<MyHolderHistory> {

    Context c;
    ArrayList<HistoryData> historyData;

    public HistoryAdapter(Context c, ArrayList<HistoryData> historyData) {
        this.c = c;
        this.historyData = historyData;
    }

    //Inzializzazione ViewHolder
    @Override
    public MyHolderHistory onCreateViewHolder(ViewGroup parent, int viewType) {
        //Creazione View Object
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_layout, parent, false);

        //Creazione Holder
        MyHolderHistory holder = new MyHolderHistory(v);

        return holder;
    }

    //Inizialiazzione Bind
    @Override
    public void onBindViewHolder(final MyHolderHistory holder, final int position) {
        holder.name.setText(historyData.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return historyData.size();
    }

}
