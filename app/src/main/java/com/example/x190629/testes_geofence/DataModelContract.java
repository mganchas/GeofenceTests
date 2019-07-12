package com.example.x190629.testes_geofence;

import android.provider.BaseColumns;

/**
 * Created by X191104 on 7/12/2019.
 */

public final class DataModelContract {


    private DataModelContract() {}

/* Inner class that defines the table contents */
    public static class DataModel implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }
}