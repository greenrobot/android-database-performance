package de.greenrobot.performance.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlDelightDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sqldelight.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_INDEX_ON_STRING =
            "CREATE INDEX indexed_string ON " + IndexedStringEntity.TABLE_NAME + "("
                    + IndexedStringEntity.INDEXED_STRING + ")";

    public SqlDelightDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SimpleEntityNotNull.CREATE_TABLE);
        db.execSQL(IndexedStringEntity.CREATE_TABLE);
        db.execSQL(CREATE_INDEX_ON_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SimpleEntityNotNull.CREATE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + IndexedStringEntity.CREATE_TABLE);
        onCreate(db);
    }
}
