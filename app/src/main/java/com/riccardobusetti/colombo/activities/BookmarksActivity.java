package com.riccardobusetti.colombo.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.riccardobusetti.colombo.R;

public class BookmarksActivity extends PlaceholderUiActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");


    }

}
