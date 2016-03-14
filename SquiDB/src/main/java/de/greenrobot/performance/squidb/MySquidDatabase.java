package de.greenrobot.performance.squidb;

import android.content.Context;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.Index;
import com.yahoo.squidb.sql.Table;

public class MySquidDatabase extends SquidDatabase {

    public static final String DATABASE_NAME = "sqldelight.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Create a new SquidDatabase
     *
     * @param context the Context, must not be null
     */
    public MySquidDatabase(Context context) {
        super(context);
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
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
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
