package com.riccardobusetti.colombo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class DBHelperHistory extends SQLiteOpenHelper {

    public DBHelperHistory(Context context) {
        super(context, ContantsHistory.DB_NAME_H, null, ContantsHistory.DB_VERSION_H);
    }

    //Creazione Table
    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL(ContantsHistory.CREATE_H_TABLE);

        } catch (Exception ex) {

            ex.printStackTrace();

        }

    }

    //Upgrade Table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + ContantsHistory.TB_NAME_H);

        onCreate(db);

    }

}
