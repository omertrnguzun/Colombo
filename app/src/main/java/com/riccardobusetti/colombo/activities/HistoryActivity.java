package com.riccardobusetti.colombo.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.adapter.HistoryAdapter;
import com.riccardobusetti.colombo.data.HistoryData;
import com.riccardobusetti.colombo.database.DBAdapterHistory;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private HistoryAdapter adapter;
    private ArrayList<HistoryData> historyDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv = (RecyclerView) findViewById(R.id.recyclerViewerHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.hasFixedSize();
        adapter = new HistoryAdapter(this, historyDatas);

        retrieve();

    }

    /**
     * Get data from DB
     */
    private void retrieve() {

        DBAdapterHistory db = new DBAdapterHistory(this);
        db.openDB();

        historyDatas.clear();

        Cursor c = db.getAllData();
        while (c.moveToNext()) {
            int id = c.getInt(0);
            String title = c.getString(1);
            String link = c.getString(2);

            HistoryData historyData = new HistoryData(id, title, link);

            historyDatas.add(historyData);
        }

        if (!(historyDatas.size() < 1)) {
            rv.setAdapter(adapter);
        }

        db.closeDB();
    }
}
