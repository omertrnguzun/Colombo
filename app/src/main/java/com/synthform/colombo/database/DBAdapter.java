package com.synthform.colombo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class DBAdapter {

    Context c;
    SQLiteDatabase db;
    DBHelper helper;

    public DBAdapter(Context c) {
        this.c = c;
        helper = new DBHelper(c);
    }

    //Apertura DataBase
    public DBAdapter openDB() {
        try {
            db = helper.getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    //Chiusura DataBase
    public DBAdapter closeDB() {
        try {
            helper.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    //Inserimento
    public long add(String name, String code, String hex) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Contants.NAME, name);
            cv.put(Contants.CODE, code);
            cv.put(Contants.HEX, hex);
            return db.insert(Contants.TB_NAME, Contants.ROW_ID, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Prendere
    public Cursor getAllData() {
        String[] colums = {Contants.ROW_ID, Contants.NAME, Contants.CODE, Contants.HEX};
        return db.query(Contants.TB_NAME, colums, null, null, null, null, null);
    }

    //Aggiornare DB
    public long update(int id, String name) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Contants.NAME, name);
            return db.update(Contants.TB_NAME, cv, Contants.ROW_ID + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Eliminare ID
    public long delete(int id) {
        try {
            return db.delete(Contants.TB_NAME, Contants.ROW_ID + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
