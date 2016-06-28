package de.greenrobot.performance.squidb;

import android.content.Context;
import com.yahoo.squidb.android.AndroidOpenHelper;
import com.yahoo.squidb.data.ISQLiteDatabase;
import com.yahoo.squidb.data.ISQLiteOpenHelper;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Index;
import com.yahoo.squidb.sql.Table;

public class MySquidDatabase extends SquidDatabase {

    public static final String DATABASE_NAME = "sqldelight.db";
    private static final int DATABASE_VERSION = 1;

    private Context context;

    /**
     * Create a new SquidDatabase
     *
     * @param context the Context, must not be null
     */
    public MySquidDatabase(Context context) {
        super();
        this.context = context;
    }

    @Override
    public String getName() {
        return DATABASE_NAME;
    }

    @Override
    protected int getVersion() {
        return DATABASE_VERSION;
    }

    @Override
    protected Table[] getTables() {
        return new Table[] { SimpleEntityNotNull.TABLE, IndexedStringEntity.TABLE };
    }

    @Override
    protected Index[] getIndexes() {
        return new Index[] {
                IndexedStringEntity.TABLE.index("string_idx", IndexedStringEntity.INDEXED_STRING)
        };
    }

    @Override
    protected boolean onUpgrade(ISQLiteDatabase db, int oldVersion, int newVersion) {
        return false;
    }

    @Override
    protected ISQLiteOpenHelper createOpenHelper(String databaseName,
            OpenHelperDelegate delegate, int version) {
        return new AndroidOpenHelper(context, databaseName, delegate, version);
    }

    /**
     * Currently inserting with existing IDs is not directly supported (it is actually bad practice
     * to use the row id), work around this by exposing {@link #insertRow(TableModel)}.
     * https://github.com/yahoo/squidb/issues/84
     */
    public void persistWithId(TableModel item) {
        insertRow(item);
    }
}
