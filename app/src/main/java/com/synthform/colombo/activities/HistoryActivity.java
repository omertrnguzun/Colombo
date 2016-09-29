package com.synthform.colombo.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.synthform.colombo.R;
import com.synthform.colombo.data.HistoryData;
import com.synthform.colombo.database.DBAdapterHistory;
import com.synthform.colombo.holder.MyHolderHistory;
import com.synthform.colombo.util.ItemClickListener;
import com.synthform.colombo.util.ItemLongClickListener;

import java.util.ArrayList;
import java.util.Collections;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private HistoryAdapter adapter;
    private ArrayList<HistoryData> historyDatas = new ArrayList<>();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        rv = (RecyclerView) findViewById(R.id.recyclerViewerHistory);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new SlideInLeftAnimator());
        adapter = new HistoryAdapter(this, historyDatas);

        retrieve();

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                deleteAll();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
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
            ScaleInAnimationAdapter alphaAdapter = new ScaleInAnimationAdapter(adapter);
            rv.setAdapter(alphaAdapter);
            Collections.reverse(historyDatas);
        }

        db.closeDB();
    }

    /**
     * Delete data from DB
     */
    private void delete(int id) {
        DBAdapterHistory db = new DBAdapterHistory(HistoryActivity.this);
        db.openDB();

        long result = db.delete(id);
        if (result > 0) {
            Toast.makeText(HistoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            retrieve();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(HistoryActivity.this, "Unable to delete", Toast.LENGTH_SHORT).show();
        }

        db.closeDB();
    }

    /**
     * Delete data from DB
     */
    private void deleteAll() {
        DBAdapterHistory db = new DBAdapterHistory(HistoryActivity.this);
        db.openDB();

        long result = db.deleteAll();
        if (result > 0) {
            Toast.makeText(HistoryActivity.this, "All list deleted", Toast.LENGTH_SHORT).show();
            retrieve();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(HistoryActivity.this, "Unable to delete", Toast.LENGTH_SHORT).show();
        }

        db.closeDB();
    }

    public class HistoryAdapter extends RecyclerView.Adapter<MyHolderHistory>{

        Context c;
        ArrayList<HistoryData> historyDatas;

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
        public void onBindViewHolder(MyHolderHistory holder, final int position) {
            if (historyDatas.get(position).getTitle().length() == 0) {
                holder.name.setText(historyDatas.get(position).getLink());
            } else {
                holder.name.setText(historyDatas.get(position).getTitle() + " | " + historyDatas.get(position).getLink());
            }

            holder.setItemLongClickListener(new ItemLongClickListener() {
                @Override
                public void onItemLongClick(View v, int pos) {
                    copyToClipBoard(historyDatas.get(pos).getLink());
                }
            });

            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                    intent.setData(Uri.parse(historyDatas.get(position).getLink()));
                    startActivity(intent);
                }
            });

            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete(historyDatas.get(position).getIdentifier());
                }
            });
        }

        @Override
        public int getItemCount() {
            return historyDatas.size();
        }

    }

    /**
     * Copy to clipboard text
     * @param text
     */
    private void copyToClipBoard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
