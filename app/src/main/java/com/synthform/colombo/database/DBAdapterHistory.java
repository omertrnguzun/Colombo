package com.synthform.colombo.database;

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
    public long add(String title, String link) {

        try {

            ContentValues cv = new ContentValues();
            cv.put(ContantsHistory.TITLE, title);
            cv.put(ContantsHistory.LINK, link);

            return db.insert(ContantsHistory.TB_NAME_H, ContantsHistory.ROW_H, cv);

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return 0;
    }

    //Prendere
    public Cursor getAllData() {

        String[] colums = {ContantsHistory.ROW_H, ContantsHistory.TITLE, ContantsHistory.LINK};

        return db.query(ContantsHistory.TB_NAME_H, colums, null, null, null, null, null);
    }

    //Aggiornare DB
    public long UPDATE(int id, String title) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(Contants.NAME, title);
            return db.update(ContantsHistory.TB_NAME_H, cv, ContantsHistory.ROW_H + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Eliminare ID
    public long delete(int id) {
        try {
            return db.delete(ContantsHistory.TB_NAME_H, ContantsHistory.ROW_H + " =?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long deleteAll() {
        try {
            return db.delete(ContantsHistory.TB_NAME_H, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
