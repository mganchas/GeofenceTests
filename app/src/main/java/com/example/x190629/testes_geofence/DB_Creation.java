package com.example.x190629.testes_geofence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by X191104 on 7/12/2019.
 */


public class DB_Creation extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_POSTS =
            "CREATE TABLE " + DataModelContract.DataModel.TABLE_NAME + " (" +
                    DataModelContract.DataModel._ID + " INTEGER PRIMARY KEY," +
                    DataModelContract.DataModel.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    DataModelContract.DataModel.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " )";
    private static final String SQL_DELETE_POSTS =
            "DROP TABLE IF EXISTS " + DataModelContract.DataModel.TABLE_NAME;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public DB_Creation(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_POSTS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_POSTS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}