package com.riccardobusetti.colombo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by riccardobusetti on 18/07/16.
 */
public class DBAdapterHistory {

    Context c;
    SQLiteDatabase db;
    DBHelperHistory helper;

    public DBAdapterHistory(Context c) {
        this.c = c;
        helper = new DBHelperHistory(c);
    }

    //Apertura DataBase
    public DBAdapterHistory openDB() {

        try {

            db = helper.getWritableDatabase();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return this;

    }

    //Chiusura DataBase
    public DBAdapterHistory closeDB() {

        try {

            helper.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return this;

    }

    //Inserimento
    public long add(String name, String code) {

        try {

            ContentValues cv = new ContentValues();
            cv.put(Contants.NAME_H, name);
            cv.put(Contants.CODE_H, code);

            return db.insert(Contants.TB_NAME_H, Contants.ROW_ID_H, cv);

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return 0;
    }

    //Prendere
    public Cursor getAllData() {

        String[] colums = {Contants.ROW_ID_H, Contants.NAME_H, Contants.CODE_H};

        return db.query(Contants.TB_NAME_H, colums, null, null, null, null, null);
    }

    //Aggiornare DB
    public long UPDATE(int id, String name) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Contants.NAME_H, name);
            return db.update(Contants.TB_NAME_H, cv, Contants.ROW_ID_H + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Eliminare ID
    public long Delete(int id) {
        try {
            return db.delete(Contants.TB_NAME_H, Contants.ROW_ID_H + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
