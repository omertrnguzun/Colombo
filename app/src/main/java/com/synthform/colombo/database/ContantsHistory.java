package com.synthform.colombo.database;

/**
 * Created by riccardobusetti on 29/07/16.
 */
public class ContantsHistory {
    //Colonne
    static final String ROW_H = "identifier";
    static final String TITLE = "title";
    static final String LINK = "link";

    //Proprietà DB
    static final String DB_NAME_H = "DB_H";
    static final String TB_NAME_H = "TB_H";
    static final int DB_VERSION_H = '1';

    //Creazione DB
    static final String CREATE_H_TABLE = "CREATE TABLE TB_H(identifier INTEGER PRIMARY KEY AUTOINCREMENT," + "title TEXT NOT NULL,link TEXT NOT NULL);";
}
