package com.riccardobusetti.colombo.database;

/**
 * Created by riccardobusetti on 23/06/16.
 */

public class Contants {

    //Colonne
    static final String ROW_ID = "id";
    static final String NAME = "name";
    static final String CODE = "code";

    static final String ROW_ID_H = "idh";
    static final String NAME_H = "title";
    static final String CODE_H = "link";

    //Proprietà DB
    static final String DB_NAME = "d_DB";
    static final String TB_NAME = "d_TB";
    static final int DB_VERSION = '1';

    static final String DB_NAME_H = "d_DB_H";
    static final String TB_NAME_H = "d_TB_H";
    static final int DB_VERSION_H = '1';

    //Creazione DB
    static final String CREATE_TB = "CREATE TABLE d_TB(id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT NOT NULL,code TEXT NOT NULL);";

    static final String CREATE_TB_H = "CREATE TABLE d_TB_H(id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT NOT NULL,code TEXT NOT NULL);";
}
