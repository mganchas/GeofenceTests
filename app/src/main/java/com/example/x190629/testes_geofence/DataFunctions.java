package com.example.x190629.testes_geofence;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import static com.example.x190629.testes_geofence.TestesGeofenceApp.getContext;

/**
 * Created by X191104 on 7/12/2019.
 */

public class DataFunctions {
    DataModelDBHelper dbHelper;
    SQLiteDatabase dbWriter;
    SQLiteDatabase dbReader;


    public  DataFunctions(){
        dbHelper = new DataModelDBHelper(getContext());
        dbWriter = dbHelper.getWritableDatabase();
        dbReader = dbHelper.getReadableDatabase();
    }

    public void insert( String title, String subtitle) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DataModelContract.DataModel.COLUMN_NAME_TITLE, title);
        values.put(DataModelContract.DataModel.COLUMN_NAME_SUBTITLE, subtitle);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = dbWriter.insert(DataModelContract.DataModel.TABLE_NAME, null, values);
    }

    public void read(){

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DataModelContract.DataModel.COLUMN_NAME_TITLE,
                DataModelContract.DataModel.COLUMN_NAME_SUBTITLE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = DataModelContract.DataModel.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "My Title" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                DataModelContract.DataModel.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = dbReader.query(
                DataModelContract.DataModel.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
    }

    public List list(){
        Cursor cursor = dbReader.
        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DataModelContract.DataModel._ID));
            itemIds.add(itemId);
        }
        cursor.close();
    }

    public void delete(){

        // Define 'where' part of query.
        String selection = DataModelContract.DataModel.COLUMN_NAME_TITLE + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { "MyTitle" };
// Issue SQL statement.
        int deletedRows = dbReader.delete(DataModelContract.DataModel.TABLE_NAME, selection, selectionArgs);
    }

    public void update(){
        // New value for one column
        String title = "MyNewTitle";
        ContentValues values = new ContentValues();
        values.put(DataModelContract.DataModel.COLUMN_NAME_TITLE, title);

// Which row to update, based on the title
        String selection = DataModelContract.DataModel.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { "MyOldTitle" };

        int count = dbWriter.update(
                DataModelDBHelper.DataModelContract.DataModel.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }



    }