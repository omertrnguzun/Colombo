package com.synthform.colombo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context) {
        super(context, Contants.DB_NAME, null, Contants.DB_VERSION);
    }

    //Creazione Table
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(Contants.CREATE_TB);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Upgrade Table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contants.TB_NAME);
        onCreate(db);
    }
}
