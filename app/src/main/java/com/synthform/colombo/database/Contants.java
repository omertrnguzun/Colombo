package com.synthform.colombo.database;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class Contants {

    //Colonne
    static final String ROW_ID = "id";
    static final String NAME = "name";
    static final String CODE = "code";
    static final String HEX = "hex";

    //Proprietà DB
    static final String DB_NAME = "d_DB";
    static final String TB_NAME = "d_TB";
    static final int DB_VERSION = '1';

    //Creazione DB
    static final String CREATE_TB = "CREATE TABLE d_TB(id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT NOT NULL,code TEXT NOT NULL, hex TEXT NOT NULL);";
}
