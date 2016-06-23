package com.riccardobusetti.colombo.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.adapter.MyAdapter;
import com.riccardobusetti.colombo.data.CardData;
import com.riccardobusetti.colombo.database.DBAdapter;

import java.util.ArrayList;

public class BookmarksActivity extends PlaceholderUiActivity {

    private RecyclerView rv;
    private MyAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    private ArrayList<CardData> cardDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        gridLayoutManager = new GridLayoutManager(BookmarksActivity.this, 2);

        rv = (RecyclerView) findViewById(R.id.recyclerViewer);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(gridLayoutManager);

        adapter = new MyAdapter(this, cardDatas);
        rv.setAdapter(adapter);

        retrieve();

    }

    private void retrieve() {

        DBAdapter db = new DBAdapter(this);
        db.openDB();

        cardDatas.clear();

        //Prendere dati
        Cursor c = db.getAllData();

        //Guardare nei dati e aggiungere ad ArrayList
        while (c.moveToNext()) {

            int id = c.getInt(0);
            String name = c.getString(1);
            String code = c.getString(2);

            CardData cardData = new CardData(id, name, code);

            //Aggiungere ad arraylist
            cardDatas.add(cardData);

        }

        //Controllo se ArrayList non è vuota
        if(!(cardDatas.size()<1)) {

            rv.setAdapter(adapter);

        }

        db.closeDB();

    }

}
